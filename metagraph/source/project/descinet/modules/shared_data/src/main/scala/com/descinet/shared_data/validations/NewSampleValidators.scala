package com.descinet.shared_data.validations

import com.descinet.shared_data.Utils.isValidURL
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import scala.annotation.unused

object NewSampleValidators {
  def validateNewSampleUpdate(
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
            ModelNotFound.invalid // This will now be found
        }
      case None =>
        validateSolutionEndogenousValues(update)
    }

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
