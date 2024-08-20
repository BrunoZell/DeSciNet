module HumanMovementModel

open System
open SCM

///// TYPES /////

type Latitude = Latitude of float
type Longitude = Longitude of float
type Position = Latitude * Longitude

///// OBSERVATIONS /////

type ObservationTypes =
    | GoogleTimeline of Google.Timeline.GoogleLocationObservation

///// SCM INTEGRATION /////

let initializeHumanMovementModel (initialObservation: GoogleLocationObservation) =
    let model = CausalModel.create()
    let initialPosition = (Latitude initialObservation.Latitude, Longitude initialObservation.Longitude)
    let initialEquation () = initialPosition
    let model = CausalModel.addVariable model "H" (Some initialPosition) (Some initialEquation)
    model

let integrateHumanObservation (model: CausalModel<Position>) (obs: GoogleLocationObservation) =
    let measurement = integrateObservation obs
    CausalModel.integrateMeasurement model "H" measurement.Value measurement.Timestamp

let updateHumanModel (model: CausalModel<Position>, elapsed: TimeSpan) =
    // For simplicity, assume we just re-evaluate the current equations
    model

///// PROBABILITY MODEL /////

let calculateProbability (expected: Position) (observed: Position) (lambda: float) : float =
    let (Latitude lat1, Longitude lon1) = expected
    let (Latitude lat2, Longitude lon2) = observed
    let distance = Math.Sqrt((lat1 - lat2) ** 2.0 + (lon1 - lon2) ** 2.0)
    Math.Exp(-lambda * distance)
