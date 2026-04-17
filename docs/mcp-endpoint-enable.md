# MCP Endpoint Deployment Guide

This tutorial has three parts:
- 1. How to deploy the MCP endpoint service
- 2. How to configure the MCP endpoint for a full-module deployment
- 3. How to configure the MCP endpoint for a single-module deployment

# 1. How to deploy the MCP endpoint service

## Step 1: Download the MCP endpoint project source code

Open the [MCP endpoint project page](https://github.com/xinnan-tech/mcp-endpoint-server) in your browser.

On that page, find the green button labeled `Code`, click it, and you'll see a `Download ZIP` button.

Click it to download the project source code archive. After downloading and extracting it, the folder may be named `mcp-endpoint-server-main`.
You need to rename it to `mcp-endpoint-server`.

## Step 2: Start the program
This project is very simple, and it is recommended to run it with docker. However, if you don't want to use docker, you can refer to [this page](https://github.com/xinnan-tech/mcp-endpoint-server/blob/main/README_dev.md) to run it from source. Below is the docker method:

```
# Enter the project source code root directory
cd mcp-endpoint-server

# Clean up caches
docker compose -f docker-compose.yml down
docker stop mcp-endpoint-server
docker rm mcp-endpoint-server
docker rmi ghcr.nju.edu.cn/xinnan-tech/mcp-endpoint-server:latest

# Start the docker container
docker compose -f docker-compose.yml up -d
# View the logs
docker logs -f mcp-endpoint-server
```

Then, the log will output something similar to:
```
250705 INFO-=====The addresses below are for control console / single-module MCP endpoint====
250705 INFO-Control console MCP parameter config: http://172.22.0.2:8004/mcp_endpoint/health?key=abc
250705 INFO-Single-module MCP endpoint: ws://172.22.0.2:8004/mcp_endpoint/mcp/?token=def
250705 INFO-=====Use based on your specific deployment. Do not leak these to anyone======
```

Please copy out the two interface addresses:

Since you are using a docker deployment, do NOT use the above addresses directly!

Since you are using a docker deployment, do NOT use the above addresses directly!

Since you are using a docker deployment, do NOT use the above addresses directly!

First, copy the addresses into a draft, find out your computer's LAN IP. For example, my computer's LAN IP is `192.168.1.25`, so
my original addresses
```
Control console MCP parameter config: http://172.22.0.2:8004/mcp_endpoint/health?key=abc
Single-module MCP endpoint: ws://172.22.0.2:8004/mcp_endpoint/mcp/?token=def
```
need to be changed to
```
Control console MCP parameter config: http://192.168.1.25:8004/mcp_endpoint/health?key=abc
Single-module MCP endpoint: ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=def
```

After the change, visit the `Control console MCP parameter config` address directly in your browser. If the browser displays JSON similar to the following, it means success:
```
{"result":{"status":"success","connections":{"tool_connections":0,"robot_connections":0,"total_connections":0}},"error":null,"id":null,"jsonrpc":"2.0"}
```

Please keep these two `interface addresses` safe; you will need them in the next step.

# 2. How to configure the MCP endpoint for a full-module deployment
First, you need to enable the MCP endpoint feature. In the control console, click `Parameter Dictionary` at the top, then click `System Feature Configuration` in the dropdown. On the page, check `MCP Endpoint`, then click `Save Configuration`. On the `Role Configuration` page, click the `Edit Features` button to see the `MCP endpoint` feature.

If you are doing a full-module deployment, log in to the control console with an admin account, click `Parameter Dictionary` at the top, and select the `Parameter Management` feature.

Then search for the parameter `server.mcp_endpoint`. Its value should currently be `null`.
Click the Edit button and paste the `Control console MCP parameter config` obtained in the previous step into the `Parameter Value` field. Then save.

If it saves successfully, everything is fine, and you can go to the agent to see the result. If it fails, it means the control console cannot reach the MCP endpoint; the most likely cause is a network firewall or the wrong LAN IP.

# 3. How to configure the MCP endpoint for a single-module deployment

If you are doing a single-module deployment, find your configuration file `data/.config.yaml`.
Search for `mcp_endpoint` in the config. If not found, add the `mcp_endpoint` configuration. For example, mine looks like this:
```
server:
  websocket: ws://your_ip_or_domain:port/xiaozhi/v1/
  http_port: 8002
log:
  log_level: INFO

# There may be more configuration here..

mcp_endpoint: your endpoint websocket address
```
Now paste the `Single-module MCP endpoint` obtained in `How to deploy the MCP endpoint service` into `mcp_endpoint`. Like this:

```
server:
  websocket: ws://your_ip_or_domain:port/xiaozhi/v1/
  http_port: 8002
log:
  log_level: INFO

# There may be more configuration here

mcp_endpoint: ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=def
```

After configuring, starting the single module will produce logs like:
```
250705[__main__]-INFO-Initialized component: vad success SileroVAD
250705[__main__]-INFO-Initialized component: asr success FunASRServer
250705[__main__]-INFO-OTA interface:         http://192.168.1.25:8002/xiaozhi/ota/
250705[__main__]-INFO-Vision analysis API:   http://192.168.1.25:8002/mcp/vision/explain
250705[__main__]-INFO-MCP endpoint:          ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=abc
250705[__main__]-INFO-Websocket address:     ws://192.168.1.25:8000/xiaozhi/v1/
250705[__main__]-INFO-=======The address above is a websocket protocol address; do NOT access it via browser=======
250705[__main__]-INFO-To test websocket, open test/test_page.html in Google Chrome
250705[__main__]-INFO-=============================================================
```

As above, if you see something like `MCP endpoint: ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=abc`, the configuration is successful.
