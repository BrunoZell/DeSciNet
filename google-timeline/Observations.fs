module Google.Timeline

type GoogleLocationObservation = {
    Latitude: float
    Longitude: float
    Timestamp: DateTime
}

// Load observations from JSON file
let loadObservations (filePath: string) : GoogleLocationObservation list =
    let json = File.ReadAllText(filePath)
    JsonSerializer.Deserialize<GoogleLocationObservation list>(json)
