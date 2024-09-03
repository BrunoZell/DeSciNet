package com.descinet.shared_data.calculated_state

import com.descinet.shared_data.types.Types._
import eu.timepit.refined.types.all.NonNegLong
import org.tessellation.schema.SnapshotOrdinal

case class CalculatedState(ordinal: SnapshotOrdinal, state: DeSciNetCalculatedState)

object CalculatedState {
  def empty: CalculatedState =
    CalculatedState(
      SnapshotOrdinal(NonNegLong(0L)),
      DeSciNetCalculatedState(
        externalVariables = Map.empty,
        externalMeasurementSequenceHeads = Map.empty,
        // targets = Map.empty,
        // bounties = Map.empty,
        models = Map.empty,
        // scores = Map.empty
      )
    )
}
