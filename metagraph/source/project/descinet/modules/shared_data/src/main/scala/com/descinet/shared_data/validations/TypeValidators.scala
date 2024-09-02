package com.descinet.shared_data.validations

import com.descinet.shared_data.Utils.isValidURL
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.SnapshotOrdinal
import scala.annotation.unused

object TypeValidators {
  def validateExogenousVariableName(
    update: NewVariable
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidExogenousVariableName.whenA(update.name.isEmpty || update.name.length > 64)

  def validateExogenousVariableUrl(
    update: NewVariable
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidExogenousVariableUrl.unlessA(isValidURL(update.dataApplicationUrlPath))

  def validateExogenousVariableL0NodeUrls(
    @unused update: NewVariable
  ): DataApplicationValidationErrorOr[Unit] =
    // InvalidExogenousVariableL0NodeUrls.whenA(update.l0NodeUrls.isEmpty || update.l0NodeUrls.exists(!isValidURL(_)))
    valid

  def validateMeasurementTimestamp(
    @unused value: SnapshotOrdinal
  ): DataApplicationValidationErrorOr[Unit] =
    // InvalidMeasurementTimestamp.whenA(value > SnapshotOrdinal.now)
    valid

  def validateTargetId(
    @unused update: NewTarget,
    @unused state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    // DuplicateTargetId.whenA(state.calculated.targets.contains(update.id))
    valid

  def validateExogenousVariablesInTarget(
    update: NewTarget
  ): DataApplicationValidationErrorOr[Unit] =
    EmptyExogenousVariablesInTarget.whenA(update.exogenousVariables.isEmpty)

  def validateBountyAmount(
    update: NewBounty
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidBountyAmount.whenA(update.amount <= 0)

  def validateBountyGrantee(
    @unused update: NewBounty
  ): DataApplicationValidationErrorOr[Unit] =
    // InvalidBountyGrantee.unlessA(update.grantee.isValid)
    valid

  def validateEndogenousVariableLabel(
    value: String
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidEndogenousVariableLabel.whenA(value.isEmpty || value.length > 64)

  def validateModelId(
    @unused update: NewModel,
    @unused state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] =
    // DuplicateModelId.whenA(state.calculated.models.contains(update.id))
    valid

  def validateModelAuthor(
    @unused update: NewModel
  ): DataApplicationValidationErrorOr[Unit] =
    // InvalidModelAuthor.unlessA(update.author.isValid)
    valid

  def validateModelExogenousVariables(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] =
    EmptyModelExogenousVariables.whenA(update.exogenousVariables.isEmpty)

  def validateModelEndogenousVariables(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] =
    EmptyModelEndogenousVariables.whenA(update.endogenousVariables.isEmpty)

  def validateModelTargetExogenousVariables(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] =
    ModelTargetExogenousVariablesMismatch.whenA(
      !update.target.exogenousVariables.forall(update.exogenousVariables.contains)
    )

  def validateModelEndogenousVariableEquations(
    @unused update: NewModel
  ): DataApplicationValidationErrorOr[Unit] =
    // Todo: Evaluate equation as Scala code
    // update.endogenousVariables.traverse_ { variable =>
    //   InvalidModelEndogenousVariableEquation.whenA(!isValidScalaCode(variable.equation))
    // }
    valid

  def validateSolutionEndogenousValues(
    update: NewSample
  ): DataApplicationValidationErrorOr[Unit] =
    EmptySolutionEndogenousValues.whenA(update.solution.endogenousValues.isEmpty)

  def validateSolutionEquations(
    @unused model: Model,
    @unused update: NewSample
  ): DataApplicationValidationErrorOr[Unit] =
    // Todo: Evaluate equation as Scala code
    // SolutionEquationMismatch.whenA(
    //   !model.endogenousVariables.forall { case (label, equation) =>
    //     evaluateEquation(equation.equation, update.solution.endogenousValues) == update.solution.endogenousValues(label.label)
    //   }
    // )
    valid

  def validateSolutionEndogenousVariables(
    model: Model,
    update: NewSample
  ): DataApplicationValidationErrorOr[Unit] =
    MissingEndogenousVariableValues.whenA(
      !model.endogenousVariables.keys.forall(label => update.solution.endogenousValues.contains(label))
    )
}
