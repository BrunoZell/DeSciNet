# Demonstration

This guide will walk you through the steps to create and evaluate a custom causal model on a local DeSciNet cluster.

For more examples, look through the [examples](./examples) directory.

For a mathematical background, see the [Framework](./Framework.md).

## Start the Metagraph

Start the metagraph backend and wait for the cluster to come online:

```bash
cd metagraph/scripts
./hydra build --run
```

## Start the Frontend

Start the frontend:

```bash
cd metagraph-frontend
yarn run dev
```

Open the browser and navigate to `http://localhost:3000`.

## Declare external variables

For this demo, we will use the [`examples/human-positioning/HumanPositioning.md`](./examples/human-positioning/HumanPositioning.md) causal model. It requires two external variables:

* `M-longitude`: The longitude of the device's location.
* `M-latitude`: The latitude of the device's location.

To declare these external variables, run:

```bash
cd metagraph-actions
node --loader ts-node/esm create-external-variable.ts -u "Human_Position_lon"
node --loader ts-node/esm create-external-variable.ts -u "Human_Position_lat"
```

You can view all external variables declared in the metagraph by visting [data-l1://data-application/variables](http://localhost:9200/data-application/variables) in your browser.

Lets retrieve the _External Variable ID_ for each of these variables:

```bash
curl -s -X GET "http://localhost:9200/data-application/variables" | jq -r '.[] | select(.[1].uniqueName == "Human_Position_lon" or .[1].uniqueName == "Human_Position_lat") | "\(. [1].uniqueName): \(. [0])"'
```

You will extract the two IDs like this:

```txt
Human_Position_lon: 4885b98e462453943f6efc749a07cec083cecf236a9f328471e936f5e12b75b1
Human_Position_lat: 753b7b507601632799ec225e95a8af54834f9502c2eecf764bcf9028298a54a1
```

## Prepare the Causal Model

Lets take the [`examples/human-positioning/models/1-walking-only/model-hgtp.json`](./examples/human-positioning/models/1-walking-only/model-hgtp.json) as a starting point and replace the `externalParameterLabels` with the IDs we just retrieved:

```json
{
    "externalParameterLabels": {
        "M_longitude": "4885b98e462453943f6efc749a07cec083cecf236a9f328471e936f5e12b75b1", // replaced to link to your longitude variable
        "M_latitude": "753b7b507601632799ec225e95a8af54834f9502c2eecf764bcf9028298a54a1" // replaced to link to your latitude variable
    },
    "internalParameterLabels": {
        "H_longitude": 0,
        "H_latitude": 1,
        "epsilon": 2
    },
    "internalVariables": [
        {
            "equation": "latest(M_longitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_longitude, t))"
        },
        {
            "equation": "latest(M_latitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_latitude, t))"
        },
        {
            "equation": "epsilon = 1.0"
        }
    ]
}
```

Save the file as `demo-model.json`.

## Upload a causal model

Now lets upload the causal model to the network for evaluation:

```bash
cd metagraph-actions
node --loader ts-node/esm upload-causal-model.ts -f "../demo-model.json"
```

The output will look like this:

```bash
> node --loader ts-node/esm upload-causal-model.ts -f "../demo-model.json"
╔════════════════════════════╗
║                            ║
║   Uploading causal model   ║
║                            ║
╚════════════════════════════╝
Account Details:
KeyTrio {
  privateKey: '97a48ed86bc2545fd605028e8adbf801a98ba5dfe22bd7da0ede7ec7c13fe4c5',
  publicKey: '04454a033a11737721b3c8f2f07962e57d951451f0361a2967ff3c974a90c5514df7c1627483c9c11fe9ff1ee648d119823f31d26a273b493b4b747f4783f56491',
  address: 'DAG1fyPH7ydk3jmx2KWBtQVkUF2dMX8oCdxYLZ6V'
}
Loaded Model Details:
{
  externalParameterLabels: {
    M_longitude: '4885b98e462453943f6efc749a07cec083cecf236a9f328471e936f5e12b75b1',
    M_latitude: '753b7b507601632799ec225e95a8af54834f9502c2eecf764bcf9028298a54a1'
  },
  internalParameterLabels: { H_longitude: 0, H_latitude: 1, epsilon: 2 },
  internalVariables: [
    {
      equation: 'H_longitude = latest(M_longitude, t) + Normal.Sample(0, epsilon * sqrt(t - latestTime(M_longitude, t)))'
    },
    {
      equation: 'H_latitude = latest(M_latitude, t) + Normal.Sample(0, epsilon * sqrt(t - latestTime(M_latitude, t)))'
    },
    { equation: 'epsilon = 1.0' }
  ]
}
Sending Action Message:
{
  value: {
    NewModel: {
      model: {
        author: 'DAG1fyPH7ydk3jmx2KWBtQVkUF2dMX8oCdxYLZ6V',
        externalParameterLabels: {
          M_longitude: '4885b98e462453943f6efc749a07cec083cecf236a9f328471e936f5e12b75b1',
          M_latitude: '753b7b507601632799ec225e95a8af54834f9502c2eecf764bcf9028298a54a1'
        },
        internalParameterLabels: { H_longitude: 0, H_latitude: 1, epsilon: 2 },
        internalVariables: [
          {
            equation: 'latest(M_longitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_longitude, t))'
          },
          {
            equation: 'latest(M_latitude, t) + randomGaussian() * epsilon * sqrt(t - latestTime(M_latitude, t))'
          },
          { equation: 'epsilon = 1.0' }
        ]
      }
    }
  },
  proofs: [
    {
      id: '454a033a11737721b3c8f2f07962e57d951451f0361a2967ff3c974a90c5514df7c1627483c9c11fe9ff1ee648d119823f31d26a273b493b4b747f4783f56491',
      signature: '304402204efea355ea4f5ac7c1c0ef3fa31c332948e27c0244412b20c26d8b1115f19ce9022012c500c9d360d5331f28707742fcf69ff1ba56e0f50d1c6fb371ac54891793c7'
    }
  ]
}
Response Data
{
  hash: 'd89c58091588f427bc5c343e30c2297733a6f2f5982a8257b3154c3d13708de0'
}
```

You can view the uploaded model in the causal model registry by visiting [data-l1://data-application/models](http://localhost:9200/data-application/models) in your browser.

## Evaluate the Model

Each model has an ID. Lets say our model ID is `f36d44383a278fbd648c5659726d20dc2c39d151116409a78c4b46ba60f9ba6f`.

Now you can query http://localhost:9200/data-application/environment/f36d44383a278fbd648c5659726d20dc2c39d151116409a78c4b46ba60f9ba6f/1725471294912, which should read in all observations of the models external variables up to UNIX millisecond timestamp `1725471294912`, and returns all measurements:

```json
{
  "M_longitude": [],
  "M_latitude": []
}
```

In this case, there are no measurements uploaded yet.

On it's own it doesn't do anything, but it is helpful to understand what data is available.

Next, lets evaluate the model by calling 


## Run a backtest

The important question now is: Does the model reflect reality?

DeSciNet answers this question by sampling the models predictions and compares them to predictions if future measurements of external variables would have been known. The difference in the two sampled probability distribution is used to compute the Kullback-Leibler divergence, which quantifies the _surprise_ of the model's predictions.

A low _surprise_ score indicates that the model is a good fit for the data, while a high _surprise_ score indicates that the model is not a good fit for the data.
