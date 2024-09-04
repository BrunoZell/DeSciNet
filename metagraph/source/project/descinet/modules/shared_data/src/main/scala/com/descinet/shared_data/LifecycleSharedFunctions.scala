package com.descinet.shared_data

import cats.data.NonEmptyList
import cats.effect.Async
import cats.syntax.all._
import com.descinet.shared_data.Utils._
import com.descinet.shared_data.combiners.Combiners._
import com.descinet.shared_data.types.Types._
import org.tessellation.schema.address.Address
// import com.descinet.shared_data.validations.Validations._
import com.descinet.shared_data.validations.NewExternalVariableValidators._
import com.descinet.shared_data.validations.AdvanceMeasurementSequenceValidators._
import com.descinet.shared_data.validations.NewModelValidators._
import com.descinet.shared_data.validations.NewSampleValidators._
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
  ): F[DataApplicationValidationErrorOr[Unit]] = 
    logger[F].info(s"Validating update AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA") >>
    Async[F].delay {
      update match {
        case newExternalVariable: NewExternalVariable =>
          validateExternalVariableUpdate(newExternalVariable, None)
        case advanceMeasurementSequence: AdvanceMeasurementSequence =>
          validateAdvanceMeasurementSequence(advanceMeasurementSequence)
        // case newTarget: NewTarget =>
        //   newTargetValidations(newTarget, None)
        // case newBounty: NewBounty =>
        //   newBountyValidations(newBounty)
        case newModel: NewModel =>
          validateNewModelUpdate(newModel)
        case newSample: NewSample =>
          validateNewSampleUpdate(newSample, None)
      }
    }

  def validateData[F[_] : Async](
    state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    updates: NonEmptyList[Signed[DeSciNetUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataApplicationValidationErrorOr[Unit]] = {
    implicit val sp: SecurityProvider[F] = context.securityProvider
    logger[F].info(s"Validating update with data: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA") >>
    updates.traverse { signedUpdate =>
        getFirstAddressFromProofs(signedUpdate.proofs)
        .flatMap { firstSigner =>
          Async[F].delay {
            signedUpdate.value match {
              case newExternalVariable: NewExternalVariable =>
                validateExternalVariableUpdate(newExternalVariable, state.some)
              case advanceMeasurementSequence: AdvanceMeasurementSequence =>
                validateAdvanceMeasurementSequence(advanceMeasurementSequence, state.some, firstSigner)
              // case newTarget: NewTarget =>
              //   newTargetValidations(newTarget, state.some)
              // case newBounty: NewBounty =>
              //   newBountyValidations(newBounty)
              case newModel: NewModel =>
                validateNewModelUpdate(newModel, state.some, firstSigner)
              case newSample: NewSample =>
                validateNewSampleUpdate(newSample, state.some)
            }
          }
        }
    }.map(_.reduce)
  }

  def combine[F[_] : Async](
    state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    updates: List[Signed[DeSciNetUpdate]]
  )(implicit context: L0NodeContext[F]): F[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]] = {
    val newStateF = DataState(DeSciNetOnChainState(Set.empty, Map.empty, Set.empty), state.calculated).pure[F]

    if (updates.isEmpty) {
      logger[F].info("Snapshot without any updates, updating the state to empty updates") >> newStateF
    } else {
      implicit val sp: SecurityProvider[F] = context.securityProvider
      logger[F].info(s"Combining snapshot with ${updates.length} updates") >>
      newStateF.flatMap(newState => {
        val (measurementUpdates, otherUpdates) = updates.partition(_.value.isInstanceOf[AdvanceMeasurementSequence])

        otherUpdates.foldLeftM(newState) { (acc, signedUpdate) => {
          signedUpdate.value match {
            case newExternalVariable: NewExternalVariable =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                logger[F].info(s"Author address for new external variable: $author") >>
                combineNewExternalVariable(newExternalVariable, acc, author).pure[F]
              }
            // case newTarget: NewTarget =>
            //   combineNewTarget(newTarget, acc).pure[F]
            // case newBounty: NewBounty =>
            //   getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
            //     combineNewBounty(newBounty, acc, author).pure[F]
            //   }
            case newModel: NewModel =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewModel(newModel, acc, author).pure[F]
              }
            case newSample: NewSample =>
              getFirstAddressFromProofs(signedUpdate.proofs).flatMap { author =>
                combineNewSample(newSample, acc, author).pure[F]
              }
            case _: AdvanceMeasurementSequence =>
              acc.pure[F] // Skip for now, will process later
          }
        }
        }.flatMap { updatedState =>
          val advanceMeasurementSequences = measurementUpdates.map(_.value.asInstanceOf[AdvanceMeasurementSequence])
          advanceMeasurementSequence(advanceMeasurementSequences, updatedState).pure[F]
        }
      })
    }
  }
}
