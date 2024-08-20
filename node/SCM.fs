module SCM

type Variable<'a> = {
    Name: string
    Value: 'a
}

type Equation<'a> = {
    Variables: Variable<'a> list
    Calculate: unit -> 'a
}

type Observation<'a> = {
    Variables: Variable<'a> list
    Timestamp: DateTime
}

type Intervention<'a> = {
    Name: string
    Apply: CausalModel<'a> -> CausalModel<'a>
}

type CausalModel<'a> = {
    Variables: Variable<'a> list
    Equations: Equation<'a> list
    Interventions: Intervention<'a> list
}

type Strategy<'a> = CausalModel<'a> -> Intervention<'a>

type World<'a> = {
    CausalModel: CausalModel<'a>
    Timestamp: DateTime
    History: World<'a> list
}

let applyIntervention (model: CausalModel<'a>) (intervention: Intervention<'a>) : CausalModel<'a> =
    intervention.Apply model

let sampleTrajectory (world: World<'a>) (strategy: Strategy<'a>) (deltaTime: TimeSpan) : World<'a> =
    // Sample a new world state by applying an intervention generated by the strategy
    let intervention = strategy world.CausalModel
    let newCausalModel = applyIntervention world.CausalModel intervention
    { CausalModel = newCausalModel; Timestamp = world.Timestamp.Add(deltaTime); History = world :: world.History }

let sampleTrajectories (world: World<'a>) (strategies: Strategy<'a> list) (deltaTime: TimeSpan) (numSamples: int) : World<'a> list =
    // Generate multiple trajectory samples by applying different strategies
    [ for _ in 1 .. numSamples do
        let strategy = strategies.[Random().Next(strategies.Length)]
        yield sampleTrajectory world strategy deltaTime ]

let integrateObservation (world: World<'a>) (observation: Observation<'a>) : World<'a> =
    // Integrate an observation into the world by updating the variables in the causal model
    let updatedVariables = world.CausalModel.Variables |> List.map (fun variable ->
        match observation.Variables |> List.tryFind (fun obsVar -> obsVar.Name = variable.Name) with
        | Some obsVar -> { variable with Value = obsVar.Value }
        | None -> variable)
    { world with CausalModel = { world.CausalModel with Variables = updatedVariables }; Timestamp = observation.Timestamp }

let quantifySurprise (world: World<'a>) (observation: Observation<'a>) (strategies: Strategy<'a> list) : float =
    let deltaTime = observation.Timestamp - world.Timestamp
    let sampledWorlds = sampleTrajectories world strategies deltaTime 1000 // or another appropriate number of samples
    let matchingWorlds = sampledWorlds |> List.filter (fun sampledWorld ->
        sampledWorld.CausalModel.Variables = observation.Variables)
    let probabilityOfObservation = float (List.length matchingWorlds) / float (List.length sampledWorlds)
    -Math.Log(probabilityOfObservation)
