open System
open System.IO
open System.Text.Json
open HumanMovementModel

// Convert HumanObservation to the general-purpose Observation<Position>
let toGeneralObservation (obs: HumanObservation) : Observation<Position> =
    { Variable = obs.Position; Timestamp = obs.Timestamp }

let simulateTrajectory (world: WorldState) (strategy: Strategy<Position>) (start: DateTime) (end: DateTime) : Timeline<Position> =
    let rec loop currentWorld currentTime timeline =
        if currentTime >= end then timeline
        else
            match strategy.NextIntervention(currentWorld, currentTime) with
            | Some intervention ->
                let updatedTimeline = applyUpdate timeline intervention.NewValue intervention.Timestamp
                loop currentWorld intervention.Timestamp updatedTimeline
            | None -> timeline
    loop world start { VariableName = "Position"; Updates = [] }

let sampleTrajectories (world: WorldState) (strategy: Strategy<Position>) (startObs: GoogleLocationObservation) (endObs: GoogleLocationObservation) : Timeline<Position> list =
    let startTime = startObs.Timestamp
    let endTime = endObs.Timestamp
    List.init 1000 (fun _ -> simulateTrajectory world strategy startTime endTime)

let epsilon = 0.001  // Small value for epsilon comparison

let isClose (pos1: Position) (pos2: Position) : bool =
    match pos1, pos2 with
    | (Latitude lat1, Longitude lon1), (Latitude lat2, Longitude lon2) ->
        abs(lat1 - lat2) < epsilon && abs(lon1 - lon2) < epsilon

let categorizeTrajectories (trajectories: Timeline<Position> list) (target: Position) =
    let reached, notReached = List.partition (fun timeline ->
        match List.tryLast timeline.Updates with
        | Some { NewValue = pos } -> isClose pos target
        | None -> false
    ) trajectories
    reached, notReached

let calculateProbability (trajectories: Timeline<Position> list) : float =
    let total = float (List.length trajectories)
    let probabilities = List.map (fun _ -> 1.0 / total) trajectories
    List.sum probabilities

let calculateSurprisal (observations: GoogleLocationObservation list) (strategy: Strategy<Position>) (initialWorld: WorldState) : float =
    let rec loop world prevObs remainingObs accSurprisal =
        match remainingObs with
        | [] -> accSurprisal
        | nextObs :: rest ->
            let trajectories = sampleTrajectories world strategy prevObs nextObs
            let reached, notReached = categorizeTrajectories trajectories (Latitude nextObs.Latitude, Longitude nextObs.Longitude)
            let probReached = calculateProbability reached
            let probNotReached = calculateProbability notReached
            let surprisal = -log probReached  // KL-divergence in this context is simply -log(p) for each observation
            loop world nextObs rest (accSurprisal + surprisal)
    match observations with
    | firstObs :: rest -> loop initialWorld firstObs rest 0.0
    | [] -> 0.0

[<EntryPoint>]
let main argv =
    if argv.Length <> 1 then
        printfn "Usage: CausalModelApp observation.json"
        1
    else
        let observationFile = argv.[0]
        let observations = Google.Timeline.loadObservations observationFile
        
        let strategy = WalkStrategy() :> Strategy<Position>
        let initialWorld = { PositionVars = Map.empty; HumidityVars = Map.empty }

        let surprisal = calculateSurprisal observations strategy initialWorld
        printfn "Total surprisal: %f" surprisal
        0
