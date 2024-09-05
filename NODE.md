# Node Docs [F#]

## How to Run

1. **Prepare Observations File**: Create an `observations.json` file with the following structure:
    ```json
    [
      {
        "latitude": 34.052235,
        "longitude": 118.243683,
        "timestamp": 1622548800000
      },
      {
        "latitude": 40.712776,
        "longitude": 74.005974,
        "timestamp": 1622635200000
      },
      {
        "latitude": 51.507351,
        "longitude": 0.127758,
        "timestamp": 1622721600000
      }
    ]
    ```

2. **Run Backend**:
    ```sh
    cd node
    dotnet run
    ```

3. **Run Frontend**:
    ```sh
    cd frontend
    yarn install
    yarn dev
    ```

4. **Load Observations in Browser**:
    - Open your browser and navigate to `http://localhost:3000`.
    - Drop the `observations.json` file into the application.

5. **Or Run Surprise Backtest Manually**:
    ```sh
    curl -X POST http://localhost:5000/backtest/model-1 -H "Content-Type: application/json" -d @observations.json
    ```

    It will output the surprise for each observation, and the total model surprise over all observations:
    ```json
    {
      "modelName": "model-1",
      "surprises": [
        {
          "latitudeSurprise": 61161.24970075105,
          "longitudeSurprise": 63742.415754987414,
          "timestamp": "2021-06-02T12:00:00",
          "totalSurprise": 124903.66545573846
        },
        {
          "latitudeSurprise": 63305.268561105564,
          "longitudeSurprise": 63937.053127532155,
          "timestamp": "2021-06-03T12:00:00",
          "totalSurprise": 252145.98714437618
        }
      ],
      "totalSurprise": 252145.98714437618
    }
    ```

## Folders

- **node**: Contains the main program and related files for running the DeSciNet application.
- **model-1**: Includes the first version of the human movement model and its related files.
- **model-2**: Contains an alternative human movement model with different assumptions and structures.
- **scm**: Holds the structural causal model (SCM) framework and related utilities.
- **google-timeline**: Manages data export from Google location history.
