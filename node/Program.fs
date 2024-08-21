open System
open MathNet.Numerics.Distributions
open SCM

[<EntryPoint>]
let main argv =
    if argv.Length <> 1 then
        printfn "Usage: CausalModelApp observation.json"
        1
    else
        let observationFile = argv.[0]
        let observations = Google.Timeline.loadObservations observationFile
        
        let i = HumanMovementModel.i
        let j = HumanMovementModel.j

        let sampleModel (i: I) (j: J) (variable: string) (timestamp: DateTime) =
            let ctx = createContext i j timestamp
            let variable = i.[variable]
            match variable.Equation ctx with
            | Probabilistic f -> Seq.init 100 (fun _ -> f())
            | Deterministic v -> Seq.singleton v

        let computeSurprise (predictedSamples: seq<float>, actualValue: float) : float =
            let predictedMean = Seq.average predictedSamples
            let predictedVariance = Seq.averageBy (fun x -> (x - predictedMean) ** 2.0) predictedSamples
            let actualVariance = 1.0 // Assuming a variance of 1 for the actual observation
            let klDivergence = 
                0.5 * (Math.Log(actualVariance / predictedVariance) + (predictedVariance + (predictedMean - actualValue) ** 2.0) / actualVariance - 1.0)
            klDivergence

        let totalSurprise = 
            observations
            |> Seq.fold (fun (j, totalSurprise) observation ->
                let j = HumanMovementModel.integrateObservation j observation
                let predictedSamples = sampleModel i j "H-latitude" observation.Timestamp
                let actualValue = observation.Latitude
                let surprise = computeSurprise(predictedSamples, actualValue)
                (j, totalSurprise + surprise)
            ) (j, 0.0)
            |> snd

        printfn "Total Surprise: %f" totalSurprise

        0