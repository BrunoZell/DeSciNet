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
  case class ExogenousVariableKey(
    sourceMetagraph        : Address,
    dataApplicationUrlPath : String,
  )

  @derive(decoder, encoder)
  case class ExogenousVariableId(
    identity: String // Hash[ExogenousVariableKey]
  )

  @derive(decoder, encoder)
  case class ExogenousVariable(
    name                   : String,
    proposer               : Address,
    sourceMetagraph        : Address,
    dataApplicationUrlPath : String,
    l0NodeUrls             : List[String],
  )

  @derive(decoder, encoder)
  case class Target(
    id                   : Long,
    exogenousVariables   : List[ExogenousVariableId],
  )

  @derive(decoder, encoder)
  case class Bounty(
    id                   : Long,
    target               : Target,
    grantee              : Address,
    originalAmount       : Long, // in DESCI
    remainingAmount      : Long, // in DESCI
  )

  @derive(decoder, encoder)
  case class MeasurementChain(
    timestamp  : SnapshotOrdinal,
    value      : Double,
    previous   : Option[String], // Hash[MeasurementChain]
  )

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
  case class NewVariable(
    name                   : String,
    sourceMetagraph        : Address,
    dataApplicationUrlPath : String,
    l0NodeUrls             : List[String],
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewTarget(
    exogenousVariables   : List[ExogenousVariableId],
  ) extends DeSciNetUpdate

  @derive(decoder, encoder)
  case class NewMeasurement(
    exogenousVariableId: ExogenousVariableId,
    value: Double,
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
    exogenousVariables: Set[ExogenousVariableId],
    measurements: Map[String, String], // TKey: Hash[ExogenousVariableKey; TValue: Hash[MeasurementChain]
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
    exogenousVariables: Map[String, ExogenousVariable], // TKey: Hash[ExogenousVariableKey]
    measurements: Map[String, MeasurementChain], // TKey: Hash[ExogenousVariableKey]
    models: Map[Long, Model],
    targets: Map[Long, Target],
    bounties: Map[Long, Bounty],
    scores: Map[Long, Score],
  ) extends DataCalculatedState
}
