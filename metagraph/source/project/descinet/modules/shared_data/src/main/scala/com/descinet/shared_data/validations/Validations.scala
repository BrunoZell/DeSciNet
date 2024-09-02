package com.descinet.shared_data.validations

import cats.syntax.all._
import cats.syntax.option.catsSyntaxOptionId
import com.descinet.shared_data.errors.Errors.valid
import com.descinet.shared_data.types.Types._
import com.descinet.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.address.Address
import org.tessellation.schema.snapshot.SnapshotOrdinal

object Validations {
  def newVariableValidations(
    update: NewVariable
  ): DataApplicationValidationErrorOr[Unit] =
    validateExogenousVariableName(update)
      .productR(validateExogenousVariableUrl(update))
      .productR(validateExogenousVariableL0NodeUrls(update))

  def newTargetValidations(
    update: NewTarget,
    maybeState: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case Some(state) =>
        validateTargetId(update, state)
          .productR(validateExogenousVariablesInTarget(update))
      case None =>
        validateExogenousVariablesInTarget(update)
    }

  def newBountyValidations(
    update: NewBounty
  ): DataApplicationValidationErrorOr[Unit] =
    validateBountyAmount(update)
      .productR(validateBountyGrantee(update))

  def newModelValidations(
    update: NewModel,
    maybeState: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case Some(state) =>
        validateModelId(update, state)
          .productR(validateModelAuthor(update))
          .productR(validateModelExogenousVariables(update))
          .productR(validateModelEndogenousVariables(update))
          .productR(validateModelTargetExogenousVariables(update))
          .productR(validateModelEndogenousVariableEquations(update))
      case None =>
        validateModelAuthor(update)
          .productR(validateModelExogenousVariables(update))
          .productR(validateModelEndogenousVariables(update))
          .productR(validateModelTargetExogenousVariables(update))
          .productR(validateModelEndogenousVariableEquations(update))
    }

  def newSampleValidations(
    update: NewSample,
    maybeState: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    maybeState match {
      case Some(state) =>
        state.calculated.models.get(update.modelId) match {
          case Some(model) =>
            validateSolutionEndogenousValues(update)
              .productR(validateSolutionEquations(model, update))
              .productR(validateSolutionEndogenousVariables(model, update))
          case None =>
            ModelNotFound.invalid
        }
      case None =>
        validateSolutionEndogenousValues(update)
    }

  def newMeasurementValidations(
    update: NewMeasurement,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] = {
    state.onChain.measurements.get(update.exogenousVariableId) match {
      // case Some(chainHead) if update.snapshotOrdinal <= chainHead.timestamp =>
      //   SnapshotOrdinalTooLow.invalid
      case _ => valid
    }
  }
}
