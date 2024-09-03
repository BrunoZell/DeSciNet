package com.descinet.shared_data.types

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.schema.address.Address
import org.tessellation.schema.SnapshotOrdinal

object Types {
  /**
   * Types
   * -----
   */

  @derive(decoder, encoder)
  case class ExternalVariable(
    uniqueName       : String,
    authority        : Address,
    valueMap         : String => Double
  )

  // vNext:
  // @derive(decoder, encoder)
  // case class ExternalVariableV2[ValueType](
  //   metagraph     : Address,
  //   valueMap      : DataState => ValueType
  // )

  @derive(decoder, encoder)
  case class MeasurementSequenceHead(
    externalVariableId  : String,          /// Hash[ExternalVariable]
    measurement         : Measurement,     /// Newly measured data
    previous            : Option[String],  /// previously measured value as: Option[Hash[MeasurementSequenceHead]]
  )

  @derive(decoder, encoder)
  case class Measurement(
    elapsed   : Long,     /// elapsed time in milliseconds since last measurement
    value     : Double,   /// measurement value
  )

  // @derive(decoder, encoder)
  // case class Target(
  //   externalVariables : List[String], // 'Value : Hash[ExternalVariable]
  // )

  // @derive(decoder, encoder)
  // case class Bounty(
  //   id                   : Long,
  //   target               : Target,
  //   grantee              : Address,
  //   originalAmount       : Long, // in DESCI
  //   remainingAmount      : Long, // in DESCI
  // )

  @derive(decoder, encoder)
  case class InternalVariable(
    /// Scala code to be run within Model Sampling Environment.
    /// Valid symbols:
    /// now: virtual timestamp t of sample evaliation as passed to Y(t)
    /// PAY.[internalVariableLabel](p)(t) : value of endogenous variable of type Y_{internalVariableLabel(p)} at time t
    /// PAX.[externalVariableLabel](p).value(n) : value of nth observation of type X_{externalVariableLabel(p)}
    /// PAX.[externalVariableLabel](p).latest : Highest observed n of type X_{externalVariableLabel(p)}
    /// PAX.[externalVariableLabel](p).d(n) : Elapsed time since previous observation of type X_{externalVariableLabel(p)}
    equation : String,
  )

  @derive(decoder, encoder)
  case class Model(
    /// Implicitly this model defines:
    /// I : index-set of endogenous variables (=all equations over all possible parameters)
    /// J : index-set of exogenous variables, as a subset of the global J made of all ExternalVariables of the DeSciNet Metagraph
    /// PA_x(i) : { j in J } for each i in I : direct exogenous parents of the i-th endogenous variable
    /// PA_y(i) : { i in I }for each i in I : direct endogenous parents of the i-th endogenous variable
    author                  : Address,
    externalParameterLabels : Map[String, String], // 'Key: equation-label; 'Value: Hash[ExternalVariable]
    internalParameterLabels : Map[String, Int], // 'Key: equation-label; 'Value: position index in this.internalVariables
    internalVariables       : List[InternalVariable],
  )

  @derive(decoder, encoder)
  case class Solution(
    endogenousValues: Map[String, Double],
  )

  @derive(decoder, encoder)
  case class Score(
    totalSurprise: Double,
  )

  /**
   * Data Updates
   * (Transactions)
   * --------------
   */

  @derive(decoder, encoder)
  sealed trait DeSciNetUpdate extends DataUpdate

  @derive(decoder, encoder)
  case class NewExternalVariable(
    uniqueName          : String,
    authority           : Address,
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class AdvanceMeasurementSequence(
    externalVariableId  : String,
    newHead             : MeasurementSequenceHead,
  ) extends DeSciNetUpdate

  // @derive(decoder, encoder)
  // case class NewTarget(
  //   externalVariables   : List[String], // 'Value : Hash[ExternalVariable]
  // ) extends DeSciNetUpdate

  // @derive(decoder, encoder)
  // case class NewBounty( 
  //   target: Target,
  //   amount: Long, // in DESCI
  // ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewModel(
    model   : Model,  /// Explicit causal assumptions
    //target  : Target, /// Target variables the model aims to explain
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewSample(
    modelId   : Long,
    solution  : Solution
  ) extends DeSciNetUpdate

  /**
   * On Chain State
   * (Snapshots)
   * --------------
   */

  @derive(decoder, encoder)
  case class DeSciNetOnChainState(
    externalVariables: Set[String], // 'Value : Hash[ExternalVariable]
    externalMeasurementSequenceHeads: Map[String, String], // 'Key : Hash[ExternalVariable]; 'Value : Hash[MeasurementSequenceHead]
    // targets: Map[Long, Target],
    // bounties: Map[Long, Bounty],
    models: Set[String], // 'Value : Hash[Model]
    // scores: Map[Long, Score],
  ) extends DataOnChainState

  /**
   * Calculated State
   * (Off-Chain)
   * ----------------
   */

  @derive(decoder, encoder)
  case class DeSciNetCalculatedState(
    externalVariables: Map[String, ExternalVariable], // 'Key: Hash[ExogenousVariable]
    externalMeasurementSequenceHeads: Map[String, MeasurementChain], // 'Key : Hash[ExternalVariable]
    // allExternalMeasurements: Map[String, List[MeasurementChain]], // 'Key : Hash[ExternalVariable], 'Value : all MeasurementChain from traversed externalMeasurementSequenceHeads[key]
    // externalMeasurementValues: Map[String, List[MeasurementValue]], // 'Key : Hash[ExternalVariable], 'Value : tuple(Double, Timestamp)
    // targets: Map[Long, Target],
    // bounties: Map[Long, Bounty],
    models: Map[String, Model], // 'Key : Hash[Model]
    // scores: Map[Long, Score],
  ) extends DataCalculatedState
}
