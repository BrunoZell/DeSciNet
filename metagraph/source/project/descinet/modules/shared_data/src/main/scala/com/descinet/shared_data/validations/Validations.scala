package com.descinet.shared_data.validations

// import cats.syntax.all._
// import com.descinet.shared_data.errors.Errors.valid
// import com.descinet.shared_data.errors.Errors._
// import com.descinet.shared_data.types.Types._
// import com.descinet.shared_data.validations.TypeValidators._
// import org.tessellation.currency.dataApplication.DataState
// import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

// object Validations {
//   def newTargetValidations(
//     update: NewTarget,
//     maybeState: Option[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]]
//   ): DataApplicationValidationErrorOr[Unit] =
//     maybeState match {
//       case Some(state) =>
//         validateTargetId(update, state)
//           .productR(validateExogenousVariablesInTarget(update))
//       case None =>
//         validateExogenousVariablesInTarget(update)
//     }

//   def newBountyValidations(
//     update: NewBounty
//   ): DataApplicationValidationErrorOr[Unit] =
//     validateBountyAmount(update)
//       .productR(validateBountyGrantee(update))
// }
