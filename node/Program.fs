open System
open System.IO
open System.Text.Json
open Giraffe
open Microsoft.AspNetCore.Builder
open Microsoft.AspNetCore.Hosting
open Microsoft.Extensions.DependencyInjection
open Microsoft.Extensions.Hosting
open MathNet.Numerics.Distributions
open SCM

let calculateSurprises (observations: seq<Google.Timeline.GoogleLocationObservation>) =
    // Sort observations by timestamp (earliest first)
    let sortedObservations = observations |> Seq.sortBy (fun obs -> obs.timestamp)
    // Initialize the model variables i and j
    let i, j = HumanMovementModel.create()

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
        | Probabilistic f -> Seq.init 3000 (fun _ -> f()) // Generate 3000 samples from the probabilistic function
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

    // List to store surprises for each observation
    let surprises = ResizeArray<_>()

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

            // Add the surprise to the list
            surprises.Add({|
                timestamp = nextObservationTimestamp
                latitudeSurprise = surpriseLatitude
                longitudeSurprise = surpriseLongitude
                totalSurprise = totalSurprise
            |})

            // Integrate the current observation into the model
            let j = HumanMovementModel.integrateObservation j observation

            (j, totalSurprise)
        ) (j, 0.0)
        |> snd

    // Return the list of surprises
    surprises

let webApp =
    choose [
        route "/backtest/model-1" >=> POST >=> fun next ctx ->
            task {
                let! body = ctx.ReadBodyFromRequestAsync()
                let observations = JsonSerializer.Deserialize<Google.Timeline.GoogleLocationObservation list>(body)
                let surprises = calculateSurprises observations
                let jsonOptions = JsonSerializerOptions(WriteIndented = true)
                let json = JsonSerializer.Serialize(surprises, jsonOptions)
                return! json |> Successful.OK |> ctx.WriteJsonAsync
            }
    ]

let configureApp (app: IApplicationBuilder) =
    app.UseGiraffe webApp

let configureServices (services: IServiceCollection) =
    services.AddGiraffe() |> ignore

[<EntryPoint>]
let main argv =
    Host.CreateDefaultBuilder(argv)
        .ConfigureWebHostDefaults(fun webHostBuilder ->
            webHostBuilder
                .Configure(configureApp)
                .ConfigureServices(configureServices)
                .UseUrls("http://localhost:5000")
                |> ignore)
        .Build()
        .Run()
    0