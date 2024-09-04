package com.descinet.l0.custom_routes

import cats.effect.Async
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import com.descinet.l0.ModelEvaluator
import com.descinet.shared_data.calculated_state.CalculatedStateService
import com.descinet.shared_data.types.Types._
import eu.timepit.refined.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io.{LongVar}
import org.http4s.server.middleware.CORS
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.routes.internal.{InternalUrlPrefix, PublicRoutes}
import org.tessellation.schema.address.Address
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

object HideLatestVar {
  def unapply(str: String): Option[Boolean] = str.toLowerCase match {
    case "hide" => Some(true)
    case "show" => Some(false)
    case _      => None
  }
}

case class CustomRoutes[F[_] : Async](calculatedStateService: CalculatedStateService[F]) extends Http4sDsl[F] with PublicRoutes[F] {
  private def getState: F[DeSciNetCalculatedState] =
    calculatedStateService.getCalculatedState.map(_.state)

  private def getAllVariables: F[Response[F]] = {
    getState.flatMap { state =>
      val allVariablesResponse = state.externalVariables.toList
      Ok(allVariablesResponse)
    }
  }

  private def getAllModels: F[Response[F]] = {
    getState.flatMap { state =>
      val allModelsResponse = state.models.toList
      Ok(allModelsResponse)
    }
  }

  private def collectExternalMeasurementsForModel(
    state: DeSciNetCalculatedState,
    model: Model,
    time: Long,
    hideLatest: Boolean
  ): Map[String, List[Measurement]] = {
    val externalMeasurements = model.externalParameterLabels.map { case (localLabel, externalVariableId) =>
      localLabel -> state.externalMeasurementSequenceHeads.get(externalVariableId)
    }.toMap

    /**
     * Traverses the linked list of MeasurementSequenceHead to collect all measurements.
     * The measurements are collected in ascending order by timestamp.
     */
    def traverseSequenceHead(head: MeasurementSequenceHead): List[Measurement] = {
      @annotation.tailrec
      def loop(current: Option[MeasurementSequenceHead], acc: List[Measurement]): List[Measurement] = {
        current match {
          case Some(h) if (hideLatest && h.measurement.timestamp < time) || (!hideLatest && h.measurement.timestamp <= time) =>
            loop(h.previous.flatMap(state.externalMeasurementSequenceHeads.get), h.measurement :: acc)
          case _ => acc
        }
      }
      loop(Some(head), Nil)
    }

    /**
     * Collects external measurements for each local label.
     * The measurements are reversed to ensure they are in descending order by timestamp.
     * This reversal is done once here to optimize the performance of subsequent operations.
     */
    externalMeasurements.map {
      case (localLabel, Some(head)) => localLabel -> traverseSequenceHead(head).reverse // Reverse once here
      case (localLabel, None) => localLabel -> List.empty[Measurement]
    }
  }

  private def getEnvironment(
    modelId: String,
    time: Long,
    hideLatest: Boolean
  ): F[Response[F]] = {
    getState.flatMap { state =>
      state.models.get(modelId) match {
        case None => NotFound(s"Model with id $modelId not found.")
        case Some(model) =>
          val traversedMeasurements = collectExternalMeasurementsForModel(state, model, time, hideLatest)
          Ok(traversedMeasurements)
      }
    }
  }

  private def evaluateModel(
    modelId: String,
    time: Long,
    hideLatest: Boolean
  ): F[Response[F]] = {
    getState.flatMap { state =>
      state.models.get(modelId) match {
        case None => NotFound(s"Model with id $modelId not found.")
        case Some(model) =>
          try {
            val externalMeasurements = collectExternalMeasurementsForModel(state, model, time, hideLatest)
            val evaluator = new ModelEvaluator(externalMeasurements, model, time)
            val results = evaluator.evaluate()
            Ok(results)
          } catch {
            case e: Exception =>
              val errorDetails = s"Error: ${e.getMessage}\nStackTrace: ${e.getStackTrace.map(_.toString).mkString("\n")}"
              InternalServerError(errorDetails)
          }
      }
    }
  }

  // private def getAllModelsGroupedByTarget: F[Response[F]] = {
  //   getState.flatMap { state =>
  //     val groupedModels = state.models.groupBy(_._2.targetVariableGroup).map {
  //       case (group, models) => group -> models.sortBy(-_._2.totalSurprise)
  //     }
  //     Ok(groupedModels)
  //   }
  // }

  private def getAllModelsByAddress(
    address: Address
  ): F[Response[F]] = {
    getState.flatMap { state =>
      val addressModels = state.models.filter { case (_, model) =>
        model.author == address
      }
      Ok(addressModels)
    }
  }

  // private def getModelEquationsById(
  //   modelId: String
  // ): F[Response[F]] = {
  //   getState.flatMap { state =>
  //     state.models.get(modelId.toLong).map { model =>
  //       Ok(model.equations)
  //     }.getOrElse(NotFound())
  //   }
  // }

  // private def getTotalSurpriseGraph(
  //   modelId: String
  // ): F[Response[F]] = {
  //   getState.flatMap { state =>
  //     state.models.get(modelId.toLong).map { model =>
  //       val totalSurpriseData = model.backtest.map { measurement =>
  //         measurement.timestamp -> measurement.totalSurprise
  //       }
  //       Ok(totalSurpriseData)
  //     }.getOrElse(NotFound())
  //   }
  // }

  // private def getConsensusSurpriseGraph(
  //   targetId: String
  // ): F[Response[F]] = {
  //   getState.flatMap { state =>
  //     val consensusSurpriseData = state.globalModelBacktest.filter(_.targetId == targetId).map { measurement =>
  //       measurement.timestamp -> measurement.consensusSurprise
  //     }
  //     Ok(consensusSurpriseData)
  //   }
  // }

  // private def getAllVariablesGroupedByTargets: F[Response[F]] = {
  //   getState.flatMap { state =>
  //     val groupedVariables = state.variables.groupBy(_.target)
  //     Ok(groupedVariables)
  //   }
  // }

  // private def rankContributorsByRewards: F[Response[F]] = {
  //   getState.flatMap { state =>
  //     val rankedContributors = state.contributors.map { contributor =>
  //       contributor.id -> (contributor.rewardsFromModel + contributor.rewardsFromSampling)
  //     }.toList.sortBy(-_._2)
  //     Ok(rankedContributors)
  //   }
  // }

  private val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "variables" => getAllVariables
    case GET -> Root / "models" => getAllModels
    case GET -> Root / "environment" / modelId / LongVar(time) / HideLatestVar(hideLatest) => getEnvironment(modelId, time, hideLatest)
    case GET -> Root / "environment" / modelId / LongVar(time) => getEnvironment(modelId, time, false) // Default hideLatest to false if parameter is absent
    case GET -> Root / "evaluate" / modelId / LongVar(time) / HideLatestVar(hideLatest) => evaluateModel(modelId, time, hideLatest)
    case GET -> Root / "evaluate" / modelId / LongVar(time) => evaluateModel(modelId, time, false) // Default hideLatest to false if parameter is absent
    // case GET -> Root / "models" / "grouped-by-target" => getAllModelsGroupedByTarget
    case GET -> Root / "models" / AddressVar(address) => getAllModelsByAddress(address)
    // case GET -> Root / "models" / modelId / "equations" => getModelEquationsById(modelId)
    // case GET -> Root / "models" / modelId / "total-surprise-graph" => getTotalSurpriseGraph(modelId)
    // case GET -> Root / "targets" / targetId / "consensus-surprise-graph" => getConsensusSurpriseGraph(targetId)
    // case GET -> Root / "variables" / "grouped-by-targets" => getAllVariablesGroupedByTargets
    // case GET -> Root / "contributors" / "ranked-by-rewards" => rankContributorsByRewards
  }

  val public: HttpRoutes[F] =
    CORS
      .policy
      .withAllowCredentials(false)
      .httpRoutes(routes)

  override protected def prefixPath: InternalUrlPrefix = "/"
}