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
    author: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    val variableKey = ExogenousVariableKey(update.sourceMetagraph, update.dataApplicationUrlPath)
    val variableId = ExogenousVariableId(Hash.fromBytes(Serializers.serializeVariableKey(variableKey)).toString)
    @unused val newExternalVariable = ExogenousVariable(update.name, author, update.sourceMetagraph, update.dataApplicationUrlPath, update.l0NodeUrls)

    val newOnChainState = state.onChain.copy(exogenousVariables = state.onChain.exogenousVariables + variableId)
    // Convert variableId to String
    val newCalculatedState = state.calculated.copy(exogenousVariables = state.calculated.exogenousVariables + (variableId.identity -> newExternalVariable))

    DataState(newOnChainState, newCalculatedState)
  }

  def combineNewTarget(
    update: NewTarget,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState]
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // Todo: Introduce global target counter to avoid collisions when targets are removed.
    val targetId = state.onChain.targets.size.toLong + 1
    val newTarget = Target(targetId, update.exogenousVariables)

    val newOnChainState = state.onChain.copy(targets = state.onChain.targets + (targetId -> newTarget))
    val newCalculatedState = state.calculated.copy(targets = state.calculated.targets + (targetId -> newTarget))

    DataState(newOnChainState, newCalculatedState)
  }

  def combineNewBounty(
    update: NewBounty,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    author: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // Todo: Introduce functional bounty key avoid collisions when bounties are removed.
    val bountyId = state.onChain.bounties.size.toLong + 1
    val newBounty = Bounty(bountyId, update.target, author, update.amount, update.amount)

    val newOnChainState = state.onChain.copy(bounties = state.onChain.bounties + (bountyId -> newBounty))
    val newCalculatedState = state.calculated.copy(bounties = state.calculated.bounties + (bountyId -> newBounty))

    DataState(newOnChainState, newCalculatedState)
  }

  def combineNewModel(
    update: NewModel,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    author: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    // Todo: Introduce global model counter to avoid collisions when models are removed.
    val modelId = state.onChain.models.size.toLong + 1
    // Ensure update.endogenousVariables is a Map[String, EndogenousVariableEquation]
    val endogenousVariables: Map[String, EndogenousVariableEquation] = update.endogenousVariables.map {
      case (key, value) => key -> EndogenousVariableEquation(value)
    }
    val newModel = Model(modelId, author, update.exogenousVariables, endogenousVariables, update.target)

    val newOnChainState = state.onChain.copy(models = state.onChain.models + (modelId -> newModel))
    val newCalculatedState = state.calculated.copy(models = state.calculated.models + (modelId -> newModel))

    DataState(newOnChainState, newCalculatedState)
  }

  def combineNewSample(
    update: NewSample,
    state : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
    @unused contributor: Address
  ): DataState[DeSciNetOnChainState, DeSciNetCalculatedState] = {
    state.calculated.models
      .get(update.modelId)
      .fold(state) { model =>
        @unused val newSolution = Solution(update.solution.endogenousValues)

        // Todo: Make solution have an effect on the models total surprise score.
        // Todo: Keep track of all sample contributors so they get paid if this model becomes the consensus model.

        val newOnChainState = state.onChain.copy(models = state.onChain.models.updated(update.modelId, model.copy(endogenousVariables = model.endogenousVariables)))
        val newCalculatedState = state.calculated.copy(models = state.calculated.models.updated(update.modelId, model.copy(endogenousVariables = model.endogenousVariables)))

        DataState(newOnChainState, newCalculatedState)
      }
  }

  def combineNewMeasurement(
    @unused updates: List[NewMeasurement],
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
}