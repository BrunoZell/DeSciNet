module Google.Timeline

open System
open System.Text.Json

type GoogleLocationObservation = {
    Latitude: float
    Longitude: float
    Timestamp: int64
}

// Load observations from JSON file
let loadObservations (filePath: string) : GoogleLocationObservation list =
    let json = System.IO.File.ReadAllText(filePath)
    JsonSerializer.Deserialize<GoogleLocationObservation list>(json)
