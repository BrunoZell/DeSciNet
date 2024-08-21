module HumanMovementModel

open System
open SCM
open MathNet.Numerics.Distributions

///// MODEL /////

let i : Map<string, EndogenousVariable> = 
    Map.ofList [
        ("H-longitude", {
            Type = typeof<float>;
            Equation = fun ctx -> 
                let m_longitude = ctx.latest "M-longitude" :?> float
                let epsilon = ctx.value "epsilon" :?> float
                let lambda = ctx.value "lambda" :?> float
                let t = ctx.t
                let delta_t = (t - (ctx.latestTime "M-longitude")).TotalSeconds
                let std_dev = epsilon * Math.Exp(-lambda * delta_t)
                Probabilistic (fun () -> 
                    let change = Normal.Sample(0.0, std_dev)
                    let sample = m_longitude + change
                    sample
                )
        })
        ("H-latitude", {
            Type = typeof<float>;
            Equation = fun ctx -> 
                let m_latitude = ctx.latest "M-latitude" :?> float
                let epsilon = ctx.value "epsilon" :?> float
                let lambda = ctx.value "lambda" :?> float
                let t = ctx.t
                let delta_t = (t - (ctx.latestTime "M-latitude")).TotalSeconds
                let std_dev = epsilon * Math.Exp(-lambda * delta_t)
                Probabilistic (fun () -> 
                    let change = Normal.Sample(0.0, std_dev)
                    let sample = m_latitude + change
                    sample
                )
         })
        ("epsilon", {Type = typeof<float>; Equation = fun _ -> Deterministic 0.5})
        ("lambda", {Type = typeof<float>; Equation = fun _ -> Deterministic 0.05})
    ]

let j : Map<string, ExogenousVariable> = 
    Map.ofList [
        ("M-longitude", {Type = typeof<float>; Measurements = []})
        ("M-latitude", {Type = typeof<float>; Measurements = []})
    ]

///// OBSERVATIONS /////
let integrateObservation (j: J) (obs: Google.Timeline.GoogleLocationObservation) =
    let timestamp = DateTimeOffset.FromUnixTimeMilliseconds(obs.Timestamp).DateTime
    j |> addMeasurement "M-longitude" timestamp (Deterministic obs.Longitude)
      |> addMeasurement "M-latitude" timestamp (Deterministic obs.Latitude)
