package com.descinet.shared_data.validations

import cats.syntax.all._
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.security.hash.Hash
import com.descinet.shared_data.serializers.Serializers
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
    InvalidExternalVariableName.whenA(update.uniqueName.isEmpty || update.uniqueName.length > 64)

  def externalVariableIdDoesNotExist(
    update: NewExternalVariable,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] = {
    val externalVariable = ExternalVariable(update.uniqueName, update.authority)
    val externalVariableId = Hash.fromBytes(Serializers.serializeExternalVariable(externalVariable)).toString
    DuplicateExternalVariableId.whenA(state.onChain.externalVariables.contains(externalVariableId))
  }
}
