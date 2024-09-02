package com.descinet.shared_data.calculated_state

import cats.effect.Ref
import cats.effect.kernel.Async
import cats.syntax.functor.toFunctorOps
import com.descinet.shared_data.types.Types._
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.tessellation.schema.SnapshotOrdinal

import java.nio.charset.StandardCharsets

trait CalculatedStateService[F[_]] {
  def getCalculatedState: F[CalculatedState]

  def setCalculatedState(
    snapshotOrdinal: SnapshotOrdinal,
    state          : DeSciNetCalculatedState
  ): F[Boolean]

  def hashCalculatedState(
    state: DeSciNetCalculatedState
  ): F[Hash]
}

object CalculatedStateService {
  def make[F[_] : Async]: F[CalculatedStateService[F]] = {
    Ref.of[F, CalculatedState](CalculatedState.empty).map { stateRef =>
      new CalculatedStateService[F] {
        override def getCalculatedState: F[CalculatedState] = stateRef.get

        override def setCalculatedState(
          snapshotOrdinal: SnapshotOrdinal,
          state          : DeSciNetCalculatedState
        ): F[Boolean] =
          stateRef.update { currentState =>
            val currentVoteCalculatedState = currentState.state

            val updatedExogenousVariables = state.exogenousVariables.foldLeft(currentVoteCalculatedState.exogenousVariables) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            val updatedMeasurements = state.measurements.foldLeft(currentVoteCalculatedState.measurements) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            val updatedModels = state.models.foldLeft(currentVoteCalculatedState.models) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            val updatedTargets = state.targets.foldLeft(currentVoteCalculatedState.targets) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            val updatedBounties = state.bounties.foldLeft(currentVoteCalculatedState.bounties) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            val updatedScores = state.scores.foldLeft(currentVoteCalculatedState.scores) {
              case (acc, (key, value)) => acc.updated(key, value)
            }

            CalculatedState(
              snapshotOrdinal,
              DeSciNetCalculatedState(
                updatedExogenousVariables,
                updatedMeasurements,
                updatedModels,
                updatedTargets,
                updatedBounties,
                updatedScores
              )
            )
          }.as(true)


        override def hashCalculatedState(
          state: DeSciNetCalculatedState
        ): F[Hash] = Async[F].delay {
          val stateAsString = state.asJson
            .deepDropNullValues
            .noSpaces

          Hash.fromBytes(stateAsString.getBytes(StandardCharsets.UTF_8))
        }
      }
    }
  }
}
