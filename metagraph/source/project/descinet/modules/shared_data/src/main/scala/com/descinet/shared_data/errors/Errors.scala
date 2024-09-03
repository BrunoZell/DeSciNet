package com.descinet.shared_data.errors

import cats.syntax.validated.catsSyntaxValidatedIdBinCompat0
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType =
    ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType =
      err.invalidNec[Unit]

    def unlessA(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) valid else invalid

    def whenA(
      cond: Boolean
    ): DataApplicationValidationType =
      if (cond) invalid else valid
  }

  // NewExternalVariableValidators Errors
  case object InvalidExternalVariableName extends DataApplicationValidationError {
    val message = "Exogenous variable name is invalid."
  }

  case object DuplicateExternalVariableId extends DataApplicationValidationError {
    val message = "External variable already exists. Try a different unique name."
  }

  // AdvanceMeasurementSequence Errors
  case object InvalidExternalVariableId extends DataApplicationValidationError {
    val message = "External variable ID is invalid."
  }
  
  case object ExternalVariableUpdateAuthorIsNotVariableAuthority extends DataApplicationValidationError {
    val message = "The update author is not the variable authority."
  }
  
  case object InvalidPreviousHead extends DataApplicationValidationError {
    val message = "The previous head is invalid."
  }

  // Target Errors
  // case object DuplicateTargetId extends DataApplicationValidationError {
  //   val message = "Target ID already exists."
  // }

  // case object EmptyExogenousVariablesInTarget extends DataApplicationValidationError {
  //   val message = "Exogenous variables list in target is empty."
  // }

  // Bounty Errors
  // case object InvalidBountyAmount extends DataApplicationValidationError {
  //   val message = "Bounty amount must be positive."
  // }

  // case object InvalidBountyGrantee extends DataApplicationValidationError {
  //   val message = "Bounty grantee address is invalid."
  // }

  // Model Errors
  case object DuplicateModelId extends DataApplicationValidationError {
    val message = "Model ID already exists."
  }

  case object InvalidModelAuthor extends DataApplicationValidationError {
    val message = "The proof address does not match the model author."
  }

  case object DuplicateParameterLabels extends DataApplicationValidationError {
    val message = "Parameter labels are not unique across external and internal parameter labels."
  }

  case object InvalidParameterLabel extends DataApplicationValidationError {
    val message = "Parameter label contains invalid characters."
  }

  case object InvalidInternalVariableIndex extends DataApplicationValidationError {
    val message = "Internal variable index is out of bounds."
  }

  case object InvalidModelEndogenousVariableEquation extends DataApplicationValidationError {
    val message = "The model endogenous variable equation is invalid."
  }

  // Solution Errors
  case object EmptySolutionEndogenousValues extends DataApplicationValidationError {
    val message = "Endogenous values map in solution is empty."
  }

  case object SolutionEquationMismatch extends DataApplicationValidationError {
    val message = "Solution equations do not resolve to the specified values."
  }

  case object MissingEndogenousVariableValues extends DataApplicationValidationError {
    val message = "Values for all endogenous variables are not provided."
  }

  // General Errors
  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }

  case object ModelNotFound extends DataApplicationValidationError {
    val message = "Model not found."
  }
}
