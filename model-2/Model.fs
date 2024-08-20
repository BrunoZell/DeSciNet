module HumanMovementModel

open CausalReasoning
open System

///// VARIABLES /////

type Latitude = Latitude of float
type Longitude = Longitude of float
type Altitude = Altitude of float

type Coordinate = {
    Latitude: Latitude
    Longitude: Longitude
}

type AbsolutePosition = {
    Coordinate: Coordinate
    Altitude: Altitude
}

type HumanPosition =
    | Absolute of AbsolutePosition
    | PassengerInPlane of Plane * Position

    member this.ToAbsolutePosition() =
        match this with
        | Absolute pos -> pos
        | PassengerInPlane (plane, relPos) ->
            { Coordinate = { Latitude = Latitude (match plane.Position.Coordinate.Latitude, relPos.Coordinate.Latitude with | Latitude lat, Latitude dLat -> lat + dLat)
                             Longitude = Longitude (match plane.Position.Coordinate.Longitude, relPos.Coordinate.Longitude with | Longitude lon, Longitude dLon -> lon + dLon) }
              Altitude = match plane.Position.Altitude, relPos.Altitude with | Altitude alt, Altitude dAlt -> Altitude (alt + dAlt) }

type Human = {
    Id: int
    Position: HumanPosition
}

type Airport = {
    Name: string
    Location: Coordinate
}

type Plane = {
    Id: int
    Position: Position
}

///// WORLD /////

type World = {
    Humans: Map<int, Human>
    Airports: Map<string, Airport>
    Planes: Map<int, Plane>
    Timestamp: System.DateTime
    History: World list
}

// Utility: Calculate distance between two absolute positions (Haversine formula)
let distance (pos1: AbsolutePosition) (pos2: AbsolutePosition) : float =
    let r = 6371e3  // Earth's radius in meters
    let toRadians deg = deg * Math.PI / 180.0
    let dLat = toRadians (pos2.Latitude - pos1.Latitude)
    let dLon = toRadians (pos2.Longitude - pos1.Longitude)
    let a = Math.Sin(dLat / 2.0) ** 2.0 +
            Math.Cos(toRadians pos1.Latitude) * Math.Cos(toRadians pos2.Latitude) *
            Math.Sin(dLon / 2.0) ** 2.0
    let c = 2.0 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1.0 - a))
    r * c

// Walking intervention: Moves human to a new position within a short distance
let walkIntervention (destination: AbsolutePosition) : Intervention<Human> = {
    Apply = fun world ->
        let updatedHumans = 
            world.Variables |> Map.map (fun _ (Variable(human)) ->
                // Directly update the human's position without using string keys
                let newPosition = { human.Position with Latitude = destination.Latitude; Longitude = destination.Longitude }
                Variable({ human with Position = newPosition })
            )
        { world with Variables = updatedHumans; Timestamp = world.Timestamp.AddMinutes(5.0) }
}

// Flying intervention: Moves human to the closest airport
let flyIntervention (airports: Airport list) : Intervention<Human> = {
    Apply = fun world ->
        let updatedHumans = 
            world.Variables |> Map.map (fun _ (Variable(human)) ->
                // Find the closest airport to the human's current position
                let closestAirport = airports |> List.minBy (fun airport -> distance human.Position airport.Location)
                let newPosition = { closestAirport.Location with Altitude = 10000.0 }
                Variable({ human with Position = newPosition })
            )
        { world with Variables = updatedHumans; Timestamp = world.Timestamp.AddHours(1.0) }
}
