# Metagraph Actions

First, navigate to the `metagraph-actions` directory and install the dependencies:

```cmd
cd metagraph-actions
yarn install
```

## Create External Variable

```cmd
node --loader ts-node/esm create-external-variable.ts -u "uniqueVariableName"
```

The varibale name must be unique with a namespace being tied to the authorities address.

Only the authority can upload measurements for this external variable.

This command generates a random private key for the authority and prints the address to the console.

## Upload Causal Model

```cmd
node --loader ts-node/esm upload-causal-model.ts -f "../examples/human-positioning/models/1-walking-only/model-hgtp.json"
```

This command generates a random private key for the author and prints the address to the console.

The model file must be a valid JSON file that matches the expected format. The author is added dynamically to the request.

See an example model file in [examples/human-positioning/models/1-walking-only/model-hgtp.json](../examples/human-positioning/models/1-walking-only/model-hgtp.json).
