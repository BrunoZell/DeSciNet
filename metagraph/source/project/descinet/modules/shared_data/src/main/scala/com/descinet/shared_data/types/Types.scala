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
  case class EndogenousVariable(
    label                  : String,
    equation               : String,
  )

  @derive(decoder, encoder)
  case class EndogenousVariableLabel(
    label    : String,
  )

  @derive(decoder, encoder)
  case class EndogenousVariableEquation(
    equation : String,
  )

  @derive(decoder, encoder)
  case class Model(
    id                   : Long,
    author               : Address,
    exogenousVariables   : List[ExogenousVariableId],
    endogenousVariables  : Map[String, EndogenousVariableEquation], // TKey: EndogenousVariableLabel
    target               : Target,
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
    uniqueName             : String,
    authority              : Address,
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class AdvanceMeasurementSequence(
    externalVariableId: String,
    newHead: MeasurementSequenceHead,
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewTarget(
    externalVariables   : List[String], // 'Value : Hash[ExternalVariable]
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewBounty( 
    target: Target,
    amount: Long, // in DESCI
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewModel(
    author               : Address,
    target               : Target,
    exogenousVariables   : List[ExogenousVariableId],
    endogenousVariables  : Map[String, String], // TKey: Label; TValue: Equation
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
    models: Map[Long, Model],
    targets: Map[Long, Target],
    bounties: Map[Long, Bounty],
    scores: Map[Long, Score],
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
    models: Map[Long, Model],
    targets: Map[Long, Target],
    bounties: Map[Long, Bounty],
    scores: Map[Long, Score],
  ) extends DataCalculatedState
}
