# DeSciNet

## Project Overview

DeSciNet is a crowd-sourced model-building network that leverages a general-purpose model selection algorithm centered around surprise quantification and arbitrary structured causal models. The core value proposition of DeSciNet is to enable the community to collaboratively build, evaluate, and select the best causal models based on their ability to predict observed data.

### Key Features

1. **Crowd-Sourced Model Building**: Users can submit different structured causal models that specify assumptions about how data is generated.
2. **Surprise Quantification**: Models are evaluated based on their ability to predict observations. The surprise is quantified as the difference between the predicted probability and the actual observation.
3. **Model Ranking**: Models are ranked based on their total surprise, with the model having the minimum surprise being considered the best, becoming the consensus model.
4. **Assumption**: Assumes consensus over a set of observations that are considered true.
5. **Networked Computation**: Nodes in the network compute predictions through these models, and the results are aggregated to quantify the total surprise for each model.

### How It Works

1. **Loading Observations**: Observations are loaded into the system, and different models are applied to these observations.
2. **Prediction and Surprise Calculation**: Each model predicts the probability of the actual observations. A low surprise indicates a good prediction, while a high surprise indicates a poor prediction.
3. **Summing Up Surprise**: The surprise for all observations is summed up to get a total surprise for the model.
4. **Model Ranking**: Models are ranked based on their total surprise, with the lowest surprise model being the best.

### How to Run

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

### Root Folders

- **node**: Contains the main program and related files for running the DeSciNet application.
- **model-1**: Includes the first version of the human movement model and its related files.
- **model-2**: Contains an alternative human movement model with different assumptions and structures.
- **scm**: Holds the structural causal model (SCM) framework and related utilities.
- **google-timeline**: Manages data export from Google location history.

### Future Directions

1. **Consensus on Observations**: The assumption of consensus on observations is managed by a Constellation Network where all L0 data types have validation criteria attached to them.
2. **Intelligent Computation Distribution**: Sampling tasks, such as KL divergence sampling, should be distributed across multiple nodes and merged back together in a networked data structure.
3. **Model Utilization**: Authors of the best models should be rewarded with DESCI tokens if they submitted a model that lead to a more surprise-minimized consensus model.
4. **Token-Based Compute Access**: The network will offer simulations and predictions through the consensus model to DESCI token holders. The more tokens you hold, the higher compute power you have, proportional to the total compute available in the network.

DeSciNet aims to create a collaborative environment for building and evaluating causal models, rewarding contributors, and providing valuable predictions and simulations to the community.
