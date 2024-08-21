open System
open MathNet.Numerics.Distributions
open SCM

[<EntryPoint>]
let main argv =
    // Check if the correct number of arguments is provided
    if argv.Length <> 1 then
        printfn "Usage: CausalModelApp observation.json"
        1
    else
        // Load and sort observations from the provided file by timestamp (earliest first)
        let observations = Google.Timeline.loadObservations argv.[0]
                        |> Seq.sortBy (fun obs -> obs.timestamp)
        
        // Initialize the model variables i and j
        let i = HumanMovementModel.i
        let j = HumanMovementModel.j

        // Integrate the earliest observation into the model
        let initialObservation = Seq.head observations
        let j = HumanMovementModel.integrateObservation j initialObservation

        // Remaining observations after the initial one
        let observations = Seq.tail observations

        // Function to sample the model's predicted distribution for a given variable at a specific timestamp
        let sampleModel (i: I) (j: J) (variable: string) (timestamp: DateTime) =
            let ctx = createContext i j timestamp
            let variable = i.[variable]
            match variable.Equation ctx with
            | Probabilistic f -> Seq.init 100 (fun _ -> f()) // Generate 100 samples from the probabilistic function
            | Deterministic v -> Seq.singleton v // Return a single value for deterministic variables

        // Function to compute the KL divergence (surprise) between predicted samples and the actual observation
        let computeSurprise (predictedSamples: seq<float>, actualValue: float) : float =
            let predictedMean = Seq.average predictedSamples
            let predictedVariance = Seq.averageBy (fun x -> (x - predictedMean) ** 2.0) predictedSamples
            let actualVariance = 1.0 // Assuming a variance of 1 for the actual observation
            // KL divergence formula for two normal distributions
            let klDivergence = 
                0.5 * (Math.Log(actualVariance / predictedVariance) + (predictedVariance + (predictedMean - actualValue) ** 2.0) / actualVariance - 1.0)
            klDivergence

        // Compute the total surprise by iterating over all remaining observations
        let totalSurprise = 
            observations
            |> Seq.fold (fun (j, totalSurprise) observation ->
                // Sample and cast the model's prediction for the variable "H-latitude" at the observation's timestamp
                let nextObservationTimestamp = DateTimeOffset.FromUnixTimeMilliseconds(observation.timestamp).DateTime
                let predictedSamplesLatitude = 
                    sampleModel i j "H-latitude" nextObservationTimestamp
                    |> Seq.map (fun x -> x :?> float)
                
                // Get the actual value of the observation for latitude
                let actualValueLatitude = observation.latitude
                // Compute the surprise for the current observation for latitude
                let surpriseLatitude = computeSurprise(predictedSamplesLatitude, actualValueLatitude)

                // Sample and cast the model's prediction for the variable "H-longitude" at the observation's timestamp
                let predictedSamplesLongitude = 
                    sampleModel i j "H-longitude" nextObservationTimestamp
                    |> Seq.map (fun x -> x :?> float)
                
                // Get the actual value of the observation for longitude
                let actualValueLongitude = observation.longitude
                // Compute the surprise for the current observation for longitude
                let surpriseLongitude = computeSurprise(predictedSamplesLongitude, actualValueLongitude)

                // Accumulate the total surprise by summing the surprises for latitude and longitude
                let totalSurprise = totalSurprise + surpriseLatitude + surpriseLongitude

                // Integrate the current observation into the model
                let j = HumanMovementModel.integrateObservation j observation

                (j, totalSurprise)
            ) (j, 0.0)
            |> snd

        // Print the total surprise
        printfn "Total Surprise: %f" totalSurprise

        0