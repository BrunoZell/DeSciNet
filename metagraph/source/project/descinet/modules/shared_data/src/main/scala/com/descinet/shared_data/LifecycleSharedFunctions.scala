package com.descinet.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.descinet.shared_data.Utils._
import com.descinet.shared_data.combiners.Combiners._
import com.descinet.shared_data.types.Types._
import com.descinet.shared_data.validations.Validations._
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.security.SecurityProvider
import org.tessellation.security.signature.Signed
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object LifecycleSharedFunctions {
  private def logger[F[_] : Async]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromName[F]("ClusterApi")

  def validateUpdate[F[_] : Async](
    update: DeSciNetUpdate
  ): F[DataApplicationValidationErrorOr[Unit]] = Async[F].delay {
    update match {
      case newExternalVariable: NewExternalVariable =>
        validateExternalVariableUpdate(newExternalVariable, None)
      case newTarget: NewTarget =>
        newTargetValidations(newTarget, None)
      case newBounty: NewBounty =>
        newBountyValidations(newBounty)
      case newModel: NewModel =>
        newModelValidations(newModel, None)
      case newSample: NewSample =>
        newSampleValidations(newSample, None)
      case newMeasurement: NewMeasurement =>
        newMeasurementValidations(newMeasurement, None)
    }
  }

  def validateData[F[_] : Async](
    state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    updates: NonEmptyList[Signed[DeSciNetUpdate]]
  )/*(implicit context: L0NodeContext[F])*/: F[DataApplicationValidationErrorOr[Unit]] = {
    // implicit val sp: SecurityProvider[F] = context.securityProvider
    updates.traverse { signedUpdate =>
      // getAllAddressesFromProofs(signedUpdate.proofs)
      //   .flatMap { addresses =>
          Async[F].delay {
            signedUpdate.value match {
              case newExternalVariable: NewExternalVariable =>
                validateExternalVariableUpdate(newExternalVariable, state.some)
              case newTarget: NewTarget =>
                newTargetValidations(newTarget, state.some)
              case newBounty: NewBounty =>
                newBountyValidations(newBounty)
              case newModel: NewModel =>
                newModelValidations(newModel, state.some)
              case newSample: NewSample =>
                newSampleValidations(newSample, state.some)
              case newMeasurement: NewMeasurement =>
                newMeasurementValidations(newMeasurement, state.some)
            }
          }
        // }
    }.map(_.reduce)
  }

  def combine[F[_] : Async](
    state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    updates: List[Signed[DeSciNetUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]] = {
    val newStateF = DataState(DeSciNetOnChainState(Set.empty, Map.empty, Map.empty, Map.empty, Map.empty, Map.empty), state.calculated).pure[F]

    if (updates.isEmpty) {
      logger.info("Snapshot without any updates, updating the state to empty updates") >> newStateF
    } else {
      implicit val sp: SecurityProvider[F] = context.securityProvider
      newStateF.flatMap(newState => {
        val (measurementUpdates, otherUpdates) = updates.partition(_.value.isInstanceOf[NewMeasurement])

        otherUpdates.foldLeftM(newState) { (acc, signedUpdate) => {
          signedUpdate.value match {
            case newExternalVariable: NewExternalVariable =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewExternalVariable(newExternalVariable, acc, author).pure[F]
              }
            case newTarget: NewTarget =>
              combineNewTarget(newTarget, acc).pure[F]
            case newBounty: NewBounty =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewBounty(newBounty, acc, author).pure[F]
              }
            case newModel: NewModel =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewModel(newModel, acc, author).pure[F]
              }
            case newSample: NewSample =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewSample(newSample, acc, author).pure[F]
              }
            case _: NewMeasurement =>
              acc.pure[F] // Skip for now, will process later
          }
        }
        }.flatMap { updatedState =>
          val newMeasurements = measurementUpdates.map(_.value.asInstanceOf[NewMeasurement])
          combineNewMeasurement(newMeasurements, updatedState).pure[F]
        }
      })
    }
  }
}
