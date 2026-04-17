# How to Build Docker Images Locally

This project now uses GitHub's automated Docker image build feature. This document is for those who want to build Docker images locally.

1. Install docker
```
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```
2. Build the Docker images
```
# Enter the project root directory
# Build the server
docker build -t xiaozhi-esp32-server:server_latest -f ./Dockerfile-server .
# Build the web
docker build -t xiaozhi-esp32-server:web_latest -f ./Dockerfile-web .

# After building, you can use docker-compose to start the project
# You need to modify docker-compose.yml to use your own built image versions
cd main/xiaozhi-server
docker compose up -d
```
