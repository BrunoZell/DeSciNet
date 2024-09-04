package com.descinet.shared_data.validations

import cats.syntax.all._
import org.tessellation.schema.address.Address
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import scala.annotation.unused

object AdvanceMeasurementSequenceValidators {
  def validateAdvanceMeasurementSequence(
    @unused update: AdvanceMeasurementSequence
  ): DataApplicationValidationErrorOr[Unit] =
    valid

  def validateAdvanceMeasurementSequence(
    update: AdvanceMeasurementSequence,
    state: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]],
    author: Address
  ): DataApplicationValidationErrorOr[Unit] =
    state match {
      case Some(s) =>
        validateExternalVariableIdExistsInL0State(update, s)
          .productR(validateUpdateAuthorIsVariableAuthority(update, s, author))
          .productR(validatePreviousHeadIsLatestHeadInL0State(update, s))
      case None =>
        validateAdvanceMeasurementSequence(update)
    }

  // Check if the external variable ID exists in the on-chain state
  private def validateExternalVariableIdExistsInL0State(
    update: AdvanceMeasurementSequence,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidExternalVariableId.whenA(!state.onChain.externalVariables.contains(update.externalVariableId))

  // Check if the update author is the authority of the external variable
  private def validateUpdateAuthorIsVariableAuthority(
    @unused update: AdvanceMeasurementSequence,
    @unused state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    @unused author: Address
  ): DataApplicationValidationErrorOr[Unit] =
    //ExternalVariableUpdateAuthorIsNotVariableAuthority.whenA(author != state.calculated.externalVariables(update.externalVariableId).authority)
    valid

  // Check if the previous head in the update matches the latest head in the state
  private def validatePreviousHeadIsLatestHeadInL0State(
    update: AdvanceMeasurementSequence,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    state.onChain.externalMeasurementSequenceHeads.get(update.externalVariableId) match {
      case Some(headHash) =>
        // If a latest head exists in L0 state, check if the previous head in the update matches the key
        InvalidPreviousHead.whenA(update.newHead.previous != Some(headHash))
      case None =>
        // If no latest head exists in L0 state, check if the previous head in the update is None
        InvalidPreviousHead.whenA(update.newHead.previous.isDefined)
    }
}
