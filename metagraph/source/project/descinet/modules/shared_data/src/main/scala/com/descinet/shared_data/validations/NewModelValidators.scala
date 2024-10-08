package com.descinet.shared_data.validations

import cats.syntax.all._
import cats.syntax.traverse._
import com.descinet.shared_data.serializers.Serializers
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash
import com.descinet.shared_data.errors.Errors._
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import scala.annotation.unused
import scala.util.matching.Regex

object NewModelValidators {
  def validateNewModelUpdate(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] = {
    // Validate that parameter labels are unique and valid
    validateParameterLabels(update)
      // Validate that internal variable indices are valid
      .productR(validateInternalVariableIndices(update))
      // Validate that internal variable equations are valid Scala code
      .productR(validateInternalVariableEquations(update))
  }

  def validateNewModelUpdate(
    update: NewModel,
    state: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]],
    proofAddress: Address
  ): DataApplicationValidationErrorOr[Unit] =
    state match {
      case Some(s) =>
        // Validate that the model's hash id is unique
        validateModelUniqueId(update, s)
          // Validate that the proof address matches the model author
          .productR(validateModelAuthor(update, proofAddress))
          // Perform state-independent validations
          .productR(validateNewModelUpdate(update))
      case None =>
        // If no state is provided, still perform the same validations except for uniqueness
        validateModelAuthor(update, proofAddress)
          .productR(validateNewModelUpdate(update))
    }

  private def validateModelUniqueId(
    update: NewModel,
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataApplicationValidationErrorOr[Unit] = {
    // Compute the model ID by hashing the JSON representation of the model
    val modelId = Hash.fromBytes(Serializers.serializeModel(update.model)).toString
    // Check if the model ID already exists in the state
    DuplicateModelId.whenA(state.onChain.models.contains(modelId))
  }

  private def validateModelAuthor(
    update: NewModel,
    proofAddress: Address
  ): DataApplicationValidationErrorOr[Unit] = {
    // Check if the proof address matches the model author
    InvalidModelAuthor.whenA(update.model.author != proofAddress)
  }

  private def validateParameterLabels(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] = {
    // Combine all keys from external and internal parameter labels
    val allKeys = update.model.externalParameterLabels.keys ++ update.model.internalParameterLabels.keys
    // Convert the combined keys to a set to ensure uniqueness
    val uniqueKeys = allKeys.toSet

    // A regex pattern to validate the keys:
    // ^ asserts position at the start of the string
    // [a-zA-Z_] matches the first character (letter or underscore)
    // [a-zA-Z0-9_-]* matches subsequent characters (letters, digits, underscores, or hyphens)
    // $ asserts position at the end of the string
    val validKeyPattern: Regex = "^[a-zA-Z_][a-zA-Z0-9_-]*$".r

    // Check for duplicate keys and invalid key patterns
    DuplicateParameterLabels.whenA(allKeys.size != uniqueKeys.size)
      .productR(InvalidParameterLabel.whenA(!uniqueKeys.forall(key => validKeyPattern.matches(key))))
  }

  private def validateInternalVariableIndices(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] = {
    // Get the set of valid indices from the internalVariables list
    // `indices` generates a range of valid indices (0 to size-1) for the list
    // `.toSet` converts this range into a set of valid indices
    val validIndices = update.model.internalVariables.indices.toSet
    
    // Get the set of indices from the internalParameterLabels map
    // `values` extracts all the indices specified in the map
    // `.toSet` converts these indices into a set
    // `-- validIndices` performs a set difference operation, removing all valid indices from the set of indices in internalParameterLabels
    // The result is a set of invalid indices (those not present in validIndices)
    val invalidIndices = update.model.internalParameterLabels.values.toSet -- validIndices
    
    // If there are any invalid indices, return an error
    // `nonEmpty` checks if the set of invalid indices is not empty
    // `InvalidInternalVariableIndex.whenA` returns an error if invalidIndices is not empty, otherwise it returns a valid result
    InvalidInternalVariableIndex.whenA(invalidIndices.nonEmpty)
  }

  private def validateInternalVariableEquations(
    update: NewModel
  ): DataApplicationValidationErrorOr[Unit] = {
    // Traverse through each internal variable and validate its equation
    update.model.internalVariables.traverse_ { variable =>
      // Check if the equation is valid Scala code
      InvalidModelEndogenousVariableEquation.whenA(!isValidScalaCode(variable.equation))
    }
  }

  private def isValidScalaCode(@unused equation: String): Boolean = {
    // Placeholder for actual Scala code validation logic
    true
  }
}
