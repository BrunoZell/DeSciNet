package com.descinet.shared_data.validations

import com.descinet.shared_data.Utils.isValidURL
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.schema.SnapshotOrdinal
import scala.annotation.unused

object NewExternalVariableValidators {
  def validateExternalVariableUpdate(
    update: NewExternalVariable,
    state: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]]
  ): DataApplicationValidationErrorOr[Unit] =
    state match {
      case Some(s) =>
        validateExternalVariableName(update)
          .productR(externalVariableIdDoesNotExist(update, s))
      case None =>
        validateExternalVariableName(update)
    }

  private def validateExternalVariableName(
    update: NewExternalVariable
  ): DataApplicationValidationErrorOr[Unit] =
    InvalidExogenousVariableName.whenA(update.uniqueName.isEmpty || update.uniqueName.length > 64)

  def externalVariableIdDoesNotExist(
    update: NewExternalVariable,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] = {
    val externalVariable = ExternalVariable(update.uniqueName, authority)
    val externalVariableId = Hash.fromBytes(Serializers.serializeVariableKey(variableKey)).toString
    DuplicateExternalVariableId.whenA(state.onChain.externalVariables.contains(externalVariableId))
  }
}
