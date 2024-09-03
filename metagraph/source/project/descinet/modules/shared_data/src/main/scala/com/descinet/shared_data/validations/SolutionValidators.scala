package com.descinet.shared_data.validations

import com.descinet.shared_data.Utils.isValidURL
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.SnapshotOrdinal
import scala.annotation.unused

object TypeValidators {
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
