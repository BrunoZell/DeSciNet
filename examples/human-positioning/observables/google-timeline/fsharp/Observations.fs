module Google.Timeline

open System
open System.Text.Json

type GoogleLocationObservation = {
    latitude: float
    longitude: float
    timestamp: int64
}

// Load observations from JSON file
let loadObservations (filePath: string) : GoogleLocationObservation list =
    let json = System.IO.File.ReadAllText(filePath)
    JsonSerializer.Deserialize<GoogleLocationObservation list>(json)
