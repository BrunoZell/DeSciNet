module HumanMovementModel

open System
open SCM
open MathNet.Numerics.Distributions

///// MODEL /////

let create () : Map<string, EndogenousVariable> * Map<string, ExogenousVariable> =
    let i : Map<string, EndogenousVariable> = 
        Map.ofList [
            ("H-longitude", {
                Type = typeof<float>;
                Equation = fun ctx -> 
                    let m_longitude = (ctx.latest "M-longitude") :?> float
                    let epsilon = (ctx.value "epsilon") :?> float
                    let t = ctx.t
                    let delta_t = (t - (ctx.latestTime "M-longitude")).TotalSeconds
                    let std_dev = epsilon * Math.Sqrt(delta_t)
                    Probabilistic (fun () -> 
                        let change = Normal.Sample(0.0, std_dev)
                        let sample = m_longitude + change
                        sample
                    )
            })
            ("H-latitude", {
                Type = typeof<float>;
                Equation = fun ctx -> 
                    let m_latitude = (ctx.latest "M-latitude") :?> float
                    let epsilon = (ctx.value "epsilon") :?> float
                    let t = ctx.t
                    let delta_t = (t - (ctx.latestTime "M-latitude")).TotalSeconds
                    let std_dev = epsilon * Math.Sqrt(delta_t)
                    Probabilistic (fun () -> 
                        let change = Normal.Sample(0.0, std_dev)
                        let sample = m_latitude + change
                        sample
                    )
            })
            ("epsilon", {Type = typeof<float>; Equation = fun _ -> Deterministic 1.2})
        ]

    let j : Map<string, ExogenousVariable> = 
        Map.ofList [
            ("M-longitude", {Type = typeof<float>; Measurements = []})
            ("M-latitude", {Type = typeof<float>; Measurements = []})
        ]

    (i, j)

///// OBSERVATIONS /////
let integrateObservation (j: J) (obs: Google.Timeline.GoogleLocationObservation) =
    let timestamp = DateTimeOffset.FromUnixTimeMilliseconds(obs.timestamp).DateTime
    j |> addMeasurement "M-longitude" timestamp obs.longitude
      |> addMeasurement "M-latitude" timestamp obs.latitude
