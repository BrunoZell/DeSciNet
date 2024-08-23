#!/bin/bash

# Define versions for easy updates

DOCKER_COMPOSE_VERSION=v2.29.2 # Check new versions at: https://github.com/docker/compose/releases
COURSIER_VERSION=v2.1.10 # Check new versions at: https://github.com/coursier/coursier/releases and https://github.com/VirtusLab/coursier-m1/releases
SCALA_VERSION=2.13.14 # Check new versions at: https://github.com/scala/scala/releases
NODE_VERSION=v20.17.0 # Check new versions at: https://github.com/nodejs/node/releases
NVM_INSTALL_SCRIPT_VERSION=v0.40.0 # Check new versions at: https://github.com/nvm-sh/nvm/releases
YQ_VERSION=v4.44.3 # Check new versions at: https://github.com/mikefarah/yq/releases
ZSH_IN_DOCKER_VERSION=v1.2.0 # Check new versions at: https://github.com/deluan/zsh-in-docker/releases

# This script assumes that the ARCH argument is passed when running the script:
# bash install-euclid-dependencies-linux.sh x86_64
ARCH=$(echo $1 | tr '[:upper:]' '[:lower:]')
DISTRO=$(echo $2 | tr '[:upper:]' '[:lower:]')

# Validate ARCH
if [[ "$ARCH" != "amd64" && "$ARCH" != "arm" ]]; then
    echo "Unsupported architecture: $ARCH"
    exit 1
fi

# Validate DISTRO
if [[ "$DISTRO" != "ubuntu" && "$DISTRO" != "debian" ]]; then
    echo "Unsupported distribution: $DISTRO"
    exit 1
fi

# Update and install necessary packages
sudo apt-get update && \
sudo apt-get upgrade -y && \
sudo apt-get install -y \
    curl \
    wget \
    tar \
    gzip \
    git \
    gnupg2 \
    jq \
    sudo \
    zsh \
    vim \
    python3-pip \
    build-essential \
    openssl \
    libclang-dev \
    libssl-dev \
    pkg-config \
    libudev-dev

# Install Docker CLI (client only, assuming hosts docker.sock is mounted)
sudo apt-get update && \
sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common lsb-release && \
curl -fsSL https://download.docker.com/linux/${DISTRO}/gpg | sudo apt-key add - && \
echo "deb [arch=${ARCH}] https://download.docker.com/linux/${DISTRO} $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list && \
sudo apt-get update && \
sudo apt-get install -y docker-ce-cli

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \
sudo chmod +x /usr/local/bin/docker-compose

# Install Coursier
if [ "$ARCH" = "arm" ]; then
    curl -fL "https://github.com/VirtusLab/coursier-m1/releases/download/${COURSIER_VERSION}/cs-aarch64-pc-linux.gz" | gzip -d > cs
elif [ "$ARCH" = "amd64" ]; then
    curl -fL "https://github.com/coursier/coursier/releases/download/${COURSIER_VERSION}/cs-x86_64-pc-linux.gz" | gzip -d > cs
else
    echo "Error: Unsupported architecture: $ARCH"
    exit 1
fi
chmod +x cs && \
sudo mv cs /usr/local/bin/cs

# Install Scala using Coursier
cs install cs && \
cs setup --yes --apps scala:${SCALA_VERSION}

# Install Giter8 using Coursier
cs install giter8

# Install rustup and cargo
curl https://sh.rustup.rs -sSf | sh -s -- -y
export PATH="/root/.cargo/bin:${PATH}"
rustup install stable && \
rustup default stable

# Install argc
cargo install argc

# Install nvm
sudo mkdir -p /usr/local/nvm
export NVM_DIR=/usr/local/nvm
export NODE_VERSION=${NODE_VERSION}
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/${NVM_INSTALL_SCRIPT_VERSION}/install.sh | bash
source $NVM_DIR/nvm.sh && nvm install $NODE_VERSION && nvm use --delete-prefix $NODE_VERSION
export NODE_PATH=$NVM_DIR/versions/node/$NODE_VERSION/bin
export PATH=$NODE_PATH:$PATH

# Install yarn
npm install -g yarn

# Install yq
YQ_BINARY=yq_linux_${ARCH}
wget https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/${YQ_BINARY}.tar.gz -O - | \
tar xz && \
sudo mv ${YQ_BINARY} /usr/bin/yq

# Install ansible
echo 'Etc/UTC' | sudo tee /etc/timezone && \
sudo ln -s /usr/share/zoneinfo/Etc/UTC /etc/localtime
sudo apt-get install -y software-properties-common
sudo add-apt-repository -y --update ppa:ansible/ansible
sudo apt-get install -y ansible
