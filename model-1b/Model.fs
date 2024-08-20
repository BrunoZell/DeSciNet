module HumanMovementModel

open System
open SCM

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
                let h_longitude = m_longitude + epsilon * Math.Exp(-lambda * (t - (ctx.latestTime "M-longitude")).TotalSeconds)
                h_longitude
        })
        ("H-latitude", {
            Type = typeof<float>;
            Equation = fun ctx -> 
                let m_latitude = ctx.latest "M-latitude" :?> float
                let epsilon = ctx.value "epsilon" :?> float
                let lambda = ctx.value "lambda" :?> float
                let t = ctx.t
                let h_latitude = m_latitude + epsilon * Math.Exp(-lambda * (t - (ctx.latestTime "M-latitude")).TotalSeconds)
                h_latitude
         })
        ("epsilon", {Type = typeof<float>; Equation = fun _ -> 0.5})
        ("lambda", {Type = typeof<float>; Equation = fun _ -> 0.05})
    ]

let j : Map<string, ExogenousVariable> = 
    Map.ofList [
        ("M-longitude", {Type = typeof<float>; Measurements = []})
        ("M-latitude", {Type = typeof<float>; Measurements = []})
    ]

///// OBSERVATIONS /////

type ObservationTypes =
    | GoogleTimeline of Google.Timeline.GoogleLocationObservation

let integrateObservation (j: J) (obs: ObservationTypes) =
    match obs with
    | GoogleTimeline googleObs ->
        let updatedJ = j
                       |> addMeasurement "M-longitude" googleObs.Timestamp googleObs.Longitude
                       |> addMeasurement "M-latitude" googleObs.Timestamp googleObs.Latitude
        updatedJ
