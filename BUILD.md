# Building

## Metagraph Frontend

From the repository root, run the following commands to start the frontend:

```bash
yarn install
yarn start-frontend
```

The frontend expects a metagraph backend running on localhost.

## Metagraph

### Install Development Dependencies

There is an installation script available under `.devcontainer/install-euclid-dependencies.sh` that will install all the dependencies for you that are required to build and run the Metagraph in Euclid.

Only Linux is supported, possibly through WSL. Usage:

```bash
bash .devcontainer/install-euclid-dependencies.sh [architecture] [distribution]
```

Where:

* `architecture` is the architecture of the host machine. Currently, only `amd64` and `arm` are supported.
* `distro` is the distribution of the host machine. Currently, only `ubuntu` and `debian` are supported.

The official docs for dependency installation are available [here](https://docs.constellationnetwork.io/sdk/elements/hydra-cli).

You can verify that the dependencies are installed correctly by running `bash .devcontainer/verify-dependencies-installed.sh` script from the repository root. If all commands succeed, all dependencies are installed correctly.

If you want to build the Metagraph with `sbt` directly on your dev machine, check the the `.devcontainer/install-sbt-build-dependencies.sh` file.

### Build the Metagraph

First ensure that the field `github_token` in the `euclid.json` file is set to a valid GitHub token which can read public package repositories.

Make sure the Docker Deamon is running so that `docker build` and `docker run` commands can execute properly.

Then from within the `metagraph/scripts` directory, run the following command to build the metagraph:

```bash
cd metagraph/scripts
./hydra build [--no_cache] [--run]
```

Where:

* `--no_cache` is an optional parameter to build the images without using the Docker build cache.
* `--run` is an optional parameter to automatically run the containers after the images are built.

If there is a `Permission denied` error, try marking `hydra` as executable:

```bash
chmod +x hydra
```

You can also build the metagraph with `sbt` directly on your dev machine. Navigate to `metagraph/source/project/descinet` and run `sbt compile`. Make sure to have `GITHUB_TOKEN` environment variable set to a valid GitHub token which can read public package repositories: `export GITHUB_TOKEN="[your_token]"`.

### Run the Metagraph

#### Starting

We have the options `start-genesis` and `start-rollback` to start the containers. This option will fail case you didn't build the images yet.

```bash
./hydra start-genesis
./hydra start-rollback   
```

After the end of `start-genesis`, you should see something like this:

```bash
######################### METAGRAPH INFO #########################

Metagraph ID: :your_metagraph_id


Container metagraph-node-1 URLs
Global L0: http://localhost:9000/node/info
Metagraph L0: http://localhost:9200/node/info
Currency L1: http://localhost:9300/node/info
Data L1: http://localhost:9400/node/info


Container metagraph-node-2 URLs
Metagraph L0: http://localhost:9210/node/info
Currency L1: http://localhost:9310/node/info
Data L1: http://localhost:9410/node/info


Container metagraph-node-3 URLs
Metagraph L0: http://localhost:9220/node/info
Currency L1: http://localhost:9320/node/info
Data L1: http://localhost:9420/node/info


Clusters URLs
Global L0: http://localhost:9000/cluster/info
Metagraph L0: http://localhost:9200/cluster/info
Currency L1: http://localhost:9300/cluster/info
Data L1: http://localhost:9400/cluster/info

####################################################################
```

You can now access the URLs and see that your containers are working properly

#### Status

We have the option `status` to show the containers status. You can call the option this way:

```bash
./hydra status   
```

#### Stopping

We have the option `stop` to stop the containers. You can call the option this way:

```bash
./hydra stop   
```

#### Destroying

We have the option `destroy` to destroy the containers. You can call the option this way:

```bash
./hydra destroy   
```

We also have the `purge` option to destroy the containers and clean all images

```bash
./hydra purge   
```

#### Logs

We have the option `logs` to show the logs of nodes per container and layer. You can call the option this way:

```bash
./hydra logs :container_name :layer_name   
```

#### Update

We have the option `update` to update the Euclid. You can call the option this way:

```bash
./hydra update   
```
