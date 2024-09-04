package com.descinet.l0.custom_routes

import cats.effect.Async
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
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
          val externalMeasurements = collectExternalMeasurementsForModel(state, model, time, hideLatest)
          
          // Log the resulting externalMeasurements
          // println(s"External Measurements: $externalMeasurements")

          // The EvaluationEnvironment contains all symbols available to structured equations.
          class EvaluationEnvironment(externalMeasurements: Map[String, List[Measurement]], model: Model) {
            private val rng = new Random()
            private var evaluatedCache: Map[String, Double] = Map()
            private var evaluationStack: Set[String] = Set()

            // Helper functions for randomness
            def randomDouble(): Double = rng.nextDouble()
            def randomGaussian(): Double = rng.nextGaussian()

            // New methods to conform to the required signatures

            /**
             * Retrieves the latest observation value (Double) for a given external variable name (exolabel) at a specified time (time).
             * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
             * This assumption holds because the lists are reversed in the collectExternalMeasurementsForModel method.
             */
            def latest(exolabel: String, time: Long): Option[Double] = {
              externalMeasurements.get(exolabel).flatMap { measurements =>
                // Find the first measurement with a timestamp <= time
                measurements.find(_.timestamp <= time).map(_.value)
              }
            }

            /**
             * Retrieves the timestamp of the last observation for a given external variable name (exolabel) at a specified time (time).
             * Assumes that the list of measurements for each exolabel is sorted in descending order by timestamp.
             * This assumption holds because the lists are reversed in the collectExternalMeasurementsForModel method.
             */
            def latestTime(exolabel: String, time: Long): Option[Long] = {
              externalMeasurements.get(exolabel).flatMap { measurements =>
                // Find the first measurement with a timestamp <= time
                measurements.find(_.timestamp <= time).map(_.timestamp)
              }
            }

            // Evaluate an endogenous variable
            def evaluateEndogenous(label: String, t: Long): Double = {
              if (evaluationStack.contains(label)) {
                throw new RuntimeException(s"Cyclic dependency detected for variable $label")
              }

              evaluatedCache.getOrElse(label, {
                evaluationStack += label
                val equation = model.internalVariables(model.internalParameterLabels(label)).equation
                val result = evaluateEquation(equation, t, model.externalParameterLabels.keySet)
                evaluatedCache += (label -> result)
                evaluationStack -= label
                result
              })
            }

            // Parse and compile Scala code from string
            def evaluateEquation(equation: String, t: Long, externalLabels: Set[String]): Double = {
              // Generate Scala code to define symbols for each external label
              val externalSymbols = externalLabels.map(label => s"val $label = \"$label\"").mkString("\n")

              // Generate Scala code to define symbols for each endogenous variable
              val endogenousSymbols = model.internalParameterLabels.keys.map { label =>
                s"def $label(t: Long): Double = evaluateEndogenous(\"$label\", t)"
              }.mkString("\n")

              val code = s"""
                |{
                |  import scala.math.{abs, acos, asin, atan, atan2, cbrt, ceil, cos, cosh, exp, floor, hypot, log, log10, max, min, pow, round, signum, sin, sinh, sqrt, tan, tanh}
                |  val randomDouble = () => ${randomDouble()} 
                |  val randomGaussian = () => ${randomGaussian()} 
                |  val t = $t 
                |  $externalSymbols
                |  $endogenousSymbols
                |  $equation
                |}
              """.stripMargin

              val tree = toolbox.parse(code)
              toolbox.eval(tree).asInstanceOf[Double]
            }
          }

          val env = new EvaluationEnvironment(externalMeasurements, model)

          // Cache for storing evaluated endogenous variables
          var evaluatedCache: Map[String, Double] = Map()

          // Main evaluation process
          val results = model.internalVariables.zipWithIndex.map { case (variable, idx) =>
            val label = model.internalParameterLabels.collectFirst { case (key, `idx`) => key }.get
            val samples = (1 to 1000).map { _ =>
              val cached = evaluatedCache.get(label)
              cached match {
                case Some(value) => value
                case None =>
                  // Evaluate the endogenous variable using the Scala code
                  val result = env.evaluateEquation(variable.equation, time, model.externalParameterLabels.keySet)
                  evaluatedCache += (label -> result)
                  result
              }
            }
            // Return the average sample value
            label -> samples.sum / samples.size
          }

          // Return the computed response as JSON
          Ok(results)
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