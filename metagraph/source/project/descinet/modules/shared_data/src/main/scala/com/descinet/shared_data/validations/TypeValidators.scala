package com.descinet.shared_data.validations

// import com.descinet.shared_data.errors.Errors._
// import com.descinet.shared_data.types.Types._
// import org.tessellation.currency.dataApplication.DataState
// import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
// import scala.annotation.unused

// object TypeValidators {
//   def validateTargetId(
//     @unused update: NewTarget,
//     @unused state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
//   ): DataApplicationValidationErrorOr[Unit] =
//     // DuplicateTargetId.whenA(state.calculated.targets.contains(update.id))
//     valid

//   def validateExogenousVariablesInTarget(
//     update: NewTarget
//   ): DataApplicationValidationErrorOr[Unit] =
//     EmptyExogenousVariablesInTarget.whenA(update.exogenousVariables.isEmpty)

//   def validateBountyAmount(
//     update: NewBounty
//   ): DataApplicationValidationErrorOr[Unit] =
//     InvalidBountyAmount.whenA(update.amount <= 0)

//   def validateBountyGrantee(
//     @unused update: NewBounty
//   ): DataApplicationValidationErrorOr[Unit] =
//     // InvalidBountyGrantee.unlessA(update.grantee.isValid)
//     valid
// }
