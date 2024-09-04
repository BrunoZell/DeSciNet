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
// import io.circe.syntax._

object BoolVar {
  def unapply(str: String): Option[Boolean] = str.toLowerCase match {
    case "true"  => Some(true)
    case "false" => Some(false)
    case _       => None
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

  private def evaluateModel(
    modelId: String,
    time: Long,
    hideLatest: Boolean
  ): F[Response[F]] = {
    getState.flatMap { state =>
      val model = state.models.get(modelId)

      // Todo: Start dynamic causal model evaluation environment here. With:
      // t = time
      // For each external variable X_j in the model, get the full measurement vector X_j(t)
      // -> when hideLatest = true, only retrieve X_j(t) for all t' < t.
      //    This should remove the observation that happened exactly at the curren time t.
      //    Which means that all variable evaluations Y_i(t) are predictions which might be different with X_j(t) known. This is to compute surprize.
      // Evaluate all Y_i(t) by default, and sample each 1000 times.
      // -> vNext: Keep a cache of evaluated Y_i(t).

      // The evaluation environment has following Scala symbols defined:
      
      /// now: virtual timestamp t as Long
      /// randomDouble : () -> scala.util.Random.nextDouble()
      /// randomGaussian : () -> scala.util.Random.nextGaussian()
      /// All labels of internal variables: "label" : (t : Long) -> Double
      /// latest(exolabel : string, t : Long) defined for all external variable names: ExternalVariable_j.Xj.length - 1
      /// latestTime(exolabel : string, t : Long) defined for all external variable names: ExternalVariable_j.Xj.length - 1

      var sample = 0.0;
      Ok(sample)
    }
  }

  // private def evaluateModel(
  //   modelId: String,
  //   time: Long,
  //   hideLatest: Boolean
  // ): IO[Response[IO]] = {
  //   getState.flatMap { state =>
  //     state.models.get(modelId) match {
  //       case None => NotFound(s"Model with id $modelId not found.")
  //       case Some(model) =>
  //         // Map externalParameterLabels to MeasurementSequenceHead
  //         val externalMeasurements = model.externalParameterLabels.map { label =>
  //           label -> state.externalMeasurementSequenceHeads.getOrElse(label, MeasurementSequenceHead.empty)
  //         }.toMap
          
  //         // Log the resulting externalMeasurements
  //         println(s"External Measurements: $externalMeasurements")
          
  //         val env = new EvaluationEnvironment()
  
  //         // Prepare an evaluation context with available random functions
  //         val toolbox = runtimeMirror(getClass.getClassLoader).mkToolBox()
  
  //         // Parse and compile Scala code from string
  //         def evaluateEquation(equation: String, t: Long): Double = {
  //           val code = s"""
  //             |{
  //             |  val randomDouble = () => ${env.randomDouble()} 
  //             |  val randomGaussian = () => ${env.randomGaussian()} 
  //             |  val t = $t 
  //             |  $equation
  //             |}
  //           """.stripMargin
  
  //           val tree = toolbox.parse(code)
  //           toolbox.eval(tree).asInstanceOf[Double]
  //         }
  
  //         // Cache for storing evaluated endogenous variables
  //         var evaluatedCache: Map[String, Double] = Map()
  
  //         // Main evaluation process
  //         val results = model.internalVariables.zipWithIndex.map { case (variable, idx) =>
  //           val label = model.internalParameterLabels.collectFirst { case (key, `idx`) => key }.get
  //           val samples = (1 to 1000).map { _ =>
  //             val cached = evaluatedCache.get(label)
  //             cached match {
  //               case Some(value) => value
  //               case None =>
  //                 // Evaluate the endogenous variable using the Scala code
  //                 val result = evaluateEquation(variable.equation, time)
  //                 evaluatedCache += (label -> result)
  //                 result
  //             }
  //           }
  //           // Return the average sample value
  //           label -> samples.sum / samples.size
  //         }
  
  //         // Return the computed response as JSON
  //         Ok(results.asJson)
  //     }
  //   }
  // }

  private def getEnvironment(
    modelId: String,
    time: Long,
    hideLatest: Boolean
  ): F[Response[F]] = {
    getState.flatMap { state =>
      state.models.get(modelId) match {
        case None => NotFound(s"Model with id $modelId not found.")
        case Some(model) =>
          // Map externalParameterLabels to Option[MeasurementSequenceHead]
          val externalMeasurements = model.externalParameterLabels.map { case (localLabel, externalVariableId) =>
            localLabel -> state.externalMeasurementSequenceHeads.get(externalVariableId)
          }.toMap

          // Traverse each sequence head
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

          // 'Key = ExternalVariable.label (model-local), 'Value = List[Measurement]
          val traversedMeasurements: Map[String, List[Measurement]] = externalMeasurements.collect {
            case (localLabel, Some(head)) => localLabel -> traverseSequenceHead(head)
          }

          // Serialize to JSON and return
          Ok(traversedMeasurements)
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
    case GET -> Root / "environment" / modelId / LongVar(time) / BoolVar(hideLatest) => getEnvironment(modelId, time, hideLatest)
    case GET -> Root / "evaluate" / modelId / LongVar(time) / BoolVar(hideLatest) => evaluateModel(modelId, time, hideLatest)
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