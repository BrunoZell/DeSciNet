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
import org.http4s.server.middleware.CORS
import org.tessellation.ext.http4s.AddressVar
import org.tessellation.routes.internal.{InternalUrlPrefix, PublicRoutes}
import org.tessellation.schema.address.Address

case class CustomRoutes[F[_] : Async](calculatedStateService: CalculatedStateService[F]) extends Http4sDsl[F] with PublicRoutes[F] {
  private def getState: F[DeSciNetCalculatedState] =
    calculatedStateService.getCalculatedState.map(_.state)

  private def getAllModels: F[Response[F]] = {
    getState.flatMap { state =>
      val allModelsResponse = state.models.toList
      Ok(allModelsResponse)
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
    case GET -> Root / "models" => getAllModels
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