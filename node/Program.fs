open System
open System.IO
open System.Text.Json
open HumanMovementModel

// Convert HumanObservation to the general-purpose Observation<Position>
let toGeneralObservation (obs: HumanObservation) : Observation<Position> =
    { Variable = obs.Position; Timestamp = obs.Timestamp }

// Application Entry Point
[<EntryPoint>]
let main argv =
    if argv.Length <> 1 then
        printfn "Usage: CausalModelApp observation.json"
        1
    else
        // Load observations
        let observationFile = argv.[0]
        let observations = loadObservations observationFile
        let generalObservations = observations |> List.map toGeneralObservation

        // Submit the quickest paths for the observations
        let submittedPaths = submitQuickestPath generalObservations airports

        // Calculate probabilities and surprise for each path
        let probMap = mapObservationProbabilities submittedPaths
        let totalSurprise = aggregateTotalSurprise probMap

        // Output the total surprise
        printfn "Total Surprise: %f" totalSurprise

        0