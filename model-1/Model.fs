module HumanMovementModel1

open System

///// VARIABLES /////

type Latitude = Latitude of float
type Longitude = Longitude of float
type Position = Latitude * Longitude

type Human = {
    Id: int
    Position: Position
}

type World = {
    Humans: Map<int, Human>
}

///// OBSERVATIONS /////
/// 
let integrateObservation (obs: GoogleLocationObservation) : Observation<Position> =
    let position = (Latitude obs.Latitude, Longitude obs.Longitude)
    { Variable = position; Timestamp = obs.Timestamp }

///// INTERVENTIONS /////

type Walk = {
    HumanId: int
    Direction: Position  // Direction is now also a tuple
    StepLength: float
    Duration: TimeSpan
}

let applyWalk (world: World) (intervention: Walk) : World =
    let human = world.Humans.[intervention.HumanId]
    let (lat, lon) = human.Position
    let (dLat, dLon) = intervention.Direction
    let newLat = lat + (dLat * intervention.StepLength)
    let newLon = lon + (dLon * intervention.StepLength)
    let newPosition = (newLat, newLon)
    let updatedHuman = { human with Position = newPosition }
    { world with Humans = world.Humans.Add(updatedHuman.Id, updatedHuman) }

///// STRATEGIES /////

let simpleWalkStrategy (world: World) : Walk =
    let random = Random()
    let direction = (Latitude (random.NextDouble() * 2.0 - 1.0), Longitude (random.NextDouble() * 2.0 - 1.0))
    let stepLength = 1.0
    let duration = TimeSpan.FromSeconds(1.0)
    {
        HumanId = 1  // Assuming we're applying this to Human 1 for simplicity
        Direction = direction
        StepLength = stepLength
        Duration = duration
    }
