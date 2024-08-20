module HumanMovementModel1

open DomainCausal

///// VARIABLES /////

type Latitude = Latitude of float
type Longitude = Longitude of float

type Coordinate = {
    Latitude: Latitude
    Longitude: Longitude
}

type Position = {
    Coordinate: Coordinate
}

type Human = {
    Id: int
    Position: Position
}

///// WORLD /////

type World = {
    Humans: Map<int, Human>
}

///// OBSERVATIONS /////

let integrateObservation (obs: GoogleLocationObservation) : Observation<Position>
    let position = {
        Coordinate = {
            Latitude = Latitude obs.Latitude
            Longitude = Longitude obs.Longitude
        }
    }
    { Variable = position; Timestamp = obs.Timestamp }

///// INTERVENTIONS /////

type Walk = {
    HumanId: int
    Direction: Coordinate
    StepLength: float
    Duration: System.TimeSpan
}

let applyWalk (world: World) (intervention: Walk) : World =
    let human = world.Humans.[intervention.HumanId]
    let newLat = match human.Position.ToAbsolutePosition().Coordinate.Latitude, intervention.Direction.Latitude with
                 | Latitude lat, Latitude dLat -> Latitude (lat + (dLat * intervention.StepLength))
    let newLong = match human.Position.ToAbsolutePosition().Coordinate.Longitude, intervention.Direction.Longitude with
                  | Longitude lon, Longitude dLon -> Longitude (lon + (dLon * intervention.StepLength))
    let newPosition = AbsolutePosition { Coordinate = { Latitude = newLat; Longitude = newLong }; Altitude = Altitude 0.0 }
    let updatedHuman = { human with Position = newPosition }
    
    // Update the human in the world
    let updatedHumans = world.Humans.Add(updatedHuman.Id, updatedHuman)
    let newWorldState = { world with Humans = updatedHumans; Timestamp = world.Timestamp.Add(intervention.Duration) }
    { newWorldState with History = world :: world.History }

let walkIntervention = {
    Name = "Walk"
    Apply = applyWalk
}

///// STRATEGIES /////

let simpleWalkStrategy (model: CausalModel<World>) : Intervention<World> =
    {
        Name = "Simple Walk"
        Apply = fun model ->
            let dx = Random().NextDouble() * 2.0 - 1.0
            let dy = Random().NextDouble() * 2.0 - 1.0
            let updatedVariables = model.Variables |> List.map (fun variable ->
                if variable.Name = "Position" then
                    let (x, y) = variable.Value
                    { variable with Value = (x + dx, y + dy) }
                else
                    variable)
            { model with Variables = updatedVariables }
    }

///// MODEL /////

let worldToCausalModel (world: World) : CausalModel<World> =
    let variables = 
        world.Humans
        |> Map.toList
        |> List.map (fun (id, human) -> 
            { Name = sprintf "Human_%d_Position" id; Value = human.Position })
    {
        Variables = variables
        Equations = []
        Interventions = [walkIntervention]
    }
