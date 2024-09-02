package com.descinet.shared_data.types

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.tessellation.currency.dataApplication.{DataCalculatedState, DataOnChainState, DataUpdate}
import org.tessellation.schema.address.Address
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.hash.Hash

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
    identity: Hash // Hash[ExogenousVariableKey]
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
    previous   : Option[Hash], // Hash[MeasurementChain]
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
    endogenousVariables  : Map[EndogenousVariableLabel, EndogenousVariableEquation],
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
    endogenousVariables  : List[EndogenousVariable],
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
    measurements: Map[ExogenousVariableId, Hash], // Hash[MeasurementChain]
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
    exogenousVariables: Map[ExogenousVariableId, ExogenousVariable],
    measurements: Map[ExogenousVariableId, MeasurementChain],
    models: Map[Long, Model],
    targets: Map[Long, Target],
    bounties: Map[Long, Bounty],
    scores: Map[Long, Score],
  ) extends DataCalculatedState
}
