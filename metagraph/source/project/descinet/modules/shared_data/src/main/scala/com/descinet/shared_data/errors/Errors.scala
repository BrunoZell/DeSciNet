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

  case object InvalidExogenousVariableName extends DataApplicationValidationError {
    val message = "Exogenous variable name is invalid."
  }

  case object InvalidExogenousVariableUrl extends DataApplicationValidationError {
    val message = "Exogenous variable URL is invalid."
  }

  case object InvalidExogenousVariableL0NodeUrls extends DataApplicationValidationError {
    val message = "L0 node URLs are invalid."
  }

  case object InvalidMeasurementTimestamp extends DataApplicationValidationError {
    val message = "Measurement timestamp is in the future."
  }

  case object DuplicateExternalVariableId extends DataApplicationValidationError {
    val message = "External variable already exists. Try a different unique name."
  }

  case object DuplicateTargetId extends DataApplicationValidationError {
    val message = "Target ID already exists."
  }

  case object EmptyExogenousVariablesInTarget extends DataApplicationValidationError {
    val message = "Exogenous variables list in target is empty."
  }

  case object InvalidBountyAmount extends DataApplicationValidationError {
    val message = "Bounty amount must be positive."
  }

  case object InvalidBountyGrantee extends DataApplicationValidationError {
    val message = "Bounty grantee address is invalid."
  }

  case object InvalidEndogenousVariableLabel extends DataApplicationValidationError {
    val message = "Endogenous variable label is invalid."
  }

  case object InvalidEndogenousVariableEquation extends DataApplicationValidationError {
    val message = "Endogenous variable equation is invalid."
  }

  case object DuplicateModelId extends DataApplicationValidationError {
    val message = "Model ID already exists."
  }

  case object InvalidModelAuthor extends DataApplicationValidationError {
    val message = "Model author address is invalid."
  }

  case object EmptyModelExogenousVariables extends DataApplicationValidationError {
    val message = "Exogenous variables list in model is empty."
  }

  case object EmptyModelEndogenousVariables extends DataApplicationValidationError {
    val message = "Endogenous variables map in model is empty."
  }

  case object ModelTargetExogenousVariablesMismatch extends DataApplicationValidationError {
    val message = "Not all exogenous variables in the target are linked in the model."
  }

  case object InvalidModelEndogenousVariableEquation extends DataApplicationValidationError {
    val message = "Endogenous variable equation does not compile as Scala code."
  }

  case object EmptySolutionEndogenousValues extends DataApplicationValidationError {
    val message = "Endogenous values map in solution is empty."
  }

  case object SolutionEquationMismatch extends DataApplicationValidationError {
    val message = "Solution equations do not resolve to the specified values."
  }

  case object MissingEndogenousVariableValues extends DataApplicationValidationError {
    val message = "Values for all endogenous variables are not provided."
  }

  case object InvalidAddress extends DataApplicationValidationError {
    val message = "Provided address different than proof"
  }

  case class InvalidFieldSize(fieldName: String, maxSize: Long) extends DataApplicationValidationError {
    val message = s"Invalid field size: $fieldName, maxSize: $maxSize"
  }

  case object SnapshotOrdinalTooLow extends DataApplicationValidationError {
    val message = "Snapshot ordinal is too low."
  }

  case object ModelNotFound extends DataApplicationValidationError {
    val message = "Model not found."
  }
}
