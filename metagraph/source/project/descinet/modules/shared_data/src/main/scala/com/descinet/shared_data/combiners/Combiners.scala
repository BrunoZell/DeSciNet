package com.descinet.shared_data.combiners

import com.descinet.shared_data.serializers.Serializers
import com.descinet.shared_data.types.Types._
import org.tessellation.currency.dataApplication.DataState
import org.tessellation.schema.address.Address
import org.tessellation.security.hash.Hash
import scala.annotation.unused

object Combiners {
  def combineNewExternalVariable(
    update: NewExternalVariable,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    authority: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // Construct new L0 type and derive ID as an UTF8-JSON Hash thereof
    val externalVariable = ExternalVariable(update.uniqueName, authority)
    val externalVariableId = Hash.fromBytes(Serializers.serializeVariableKey(variableKey)).toString

    // Add external variable ID to onChain state
    val newOnChainState = state.onChain.copy(externalVariables = state.onChain.externalVariables + externalVariableId)

    // Add external variable to calculated state, indexed by ID
    val newCalculatedState = state.calculated.copy(externalVariables = state.calculated.externalVariables + (externalVariableId -> externalVariable))

    DataState(newOnChainState, newCalculatedState)
  }

  def advanceMeasurementSequence(
    @unused updates: List[AdvanceMeasurementSequence],
    state: DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // Validations.scala:newMeasurementValidations ensures all NewMeasurements snapshotOrdinals are greater than the states chainHead timestamp.
    // val groupedByOrdinal = updates.groupBy(_.snapshotOrdinal)

    // val newMeasurements = groupedByOrdinal.flatMap { case (ordinal, measurements) =>
    //   val values = measurements.map(_.value).distinct
    //   if (values.size == 1) {
    //     val previousHash = state.onChain.measurements.get(measurements.head.exogenousVariableId)
    //     Some(measurements.head.exogenousVariableId -> MeasurementChain(ordinal, values.head, previousHash))
    //   } else {
    //     // Log consensus error
    //     println(s"Consensus error at snapshot ordinal $ordinal for exogenous variable ${measurements.head.exogenousVariableId}")
    //     None
    //   }
    // }

    // val newOnChainState = state.onChain.copy(measurements = state.onChain.measurements ++ newMeasurements)
    // val newCalculatedState = state.calculated.copy(measurements = state.calculated.measurements ++ newMeasurements)

    // DataState(newOnChainState, newCalculatedState)
    state
  }

  // def combineNewTarget(
  //   update: NewTarget,
  //   state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  // ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
  //   // Todo: Introduce global target counter to avoid collisions when targets are removed.
  //   val targetId = state.onChain.targets.size.toLong + 1
  //   val newTarget = Target(targetId, update.exogenousVariables)

  //   val newOnChainState = state.onChain.copy(targets = state.onChain.targets + (targetId -> newTarget))
  //   val newCalculatedState = state.calculated.copy(targets = state.calculated.targets + (targetId -> newTarget))

  //   DataState(newOnChainState, newCalculatedState)
  // }

  // def combineNewBounty(
  //   update: NewBounty,
  //   state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
  //   author: Address
  // ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
  //   // Todo: Introduce functional bounty key avoid collisions when bounties are removed.
  //   val bountyId = state.onChain.bounties.size.toLong + 1
  //   val newBounty = Bounty(bountyId, update.target, author, update.amount, update.amount)

  //   val newOnChainState = state.onChain.copy(bounties = state.onChain.bounties + (bountyId -> newBounty))
  //   val newCalculatedState = state.calculated.copy(bounties = state.calculated.bounties + (bountyId -> newBounty))

  //   DataState(newOnChainState, newCalculatedState)
  // }

  def combineNewModel(
    update: NewModel,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    author: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    val modelId = Hash.fromBytes(update.model.asJson.noSpaces.getBytes).toString

    // Create the new model using the updated Model type
    val newModel = Model(
      author = author,
      externalParameterLabels = update.model.externalParameterLabels,
      internalParameterLabels = update.model.internalParameterLabels,
      internalVariables = update.model.internalVariables
    )

    // Update the onChain state with the new model
    val newOnChainState = state.onChain.copy(models = state.onChain.models + modelId)
    
    // Update the calculated state with the new model
    val newCalculatedState = state.calculated.copy(models = state.calculated.models + (modelId -> newModel))

    DataState(newOnChainState, newCalculatedState)
  }

  def combineNewSample(
    @unused update: NewSample,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    @unused contributor: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // state.calculated.models
    //   .get(update.modelId)
    //   .fold(state) { model =>
    //     @unused val newSolution = Solution(update.solution.endogenousValues)

    //     // Todo: Make solution have an effect on the models total surprise score.
    //     // Todo: Keep track of all sample contributors so they get paid if this model becomes the consensus model.

    //     val newOnChainState = state.onChain.copy(models = state.onChain.models.updated(update.modelId, model.copy(endogenousVariables = model.endogenousVariables)))
    //     val newCalculatedState = state.calculated.copy(models = state.calculated.models.updated(update.modelId, model.copy(endogenousVariables = model.endogenousVariables)))

    //     DataState(newOnChainState, newCalculatedState)
    //   }

    state
  }
}