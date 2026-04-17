# MCP Endpoint Usage Guide

This tutorial uses Brother Xia's open-source MCP calculator feature as an example to show how to integrate your custom MCP service into your own endpoint.

The prerequisite for this tutorial is that your `xiaozhi-server` has already enabled the MCP endpoint feature. If you haven't enabled it, see [this tutorial](./mcp-endpoint-enable.md) first.

# How to connect a simple MCP feature (e.g., a calculator) to an agent

### If you're using a full-module deployment
If you're using a full-module deployment, go to the control console, Agent Management, and click `Configure Role`. To the right of `Intent Recognition` there's an `Edit Features` button.

Click that button. At the bottom of the popup, there's `MCP Endpoint`. Normally it shows this agent's `MCP endpoint address`. Next, we'll extend this agent with a calculator feature based on MCP.

This `MCP endpoint address` is important; you will use it shortly.

### If you're using a single-module deployment
If you're using a single-module deployment, and you've configured the MCP endpoint address in the config, then during startup the single module will output logs like:
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

As above, the `ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=abc` after `MCP endpoint:` is your `MCP endpoint address`.

This `MCP endpoint address` is important; you will use it shortly.

## Step 1: Download Brother Xia's MCP calculator project source code

Open the [calculator project](https://github.com/78/mcp-calculator) written by Brother Xia in your browser.

On the page, find the green button labeled `Code`, click it, and you'll see a `Download ZIP` button.

Click it to download the project source code archive. After downloading and extracting, the folder may be named `mcp-calculatorr-main`.
You need to rename it to `mcp-calculator`. Next, use the command line to enter the project directory and install the dependencies.


```bash
# Enter the project directory
cd mcp-calculator

conda remove -n mcp-calculator --all -y
conda create -n mcp-calculator python=3.10 -y
conda activate mcp-calculator

pip install -r requirements.txt
```

## Step 2: Start it

Before starting, copy the MCP endpoint address from your agent in the control console.

For example, my agent's MCP address is
```
ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=abc
```

Run the following command:

```bash
export MCP_ENDPOINT=ws://192.168.1.25:8004/mcp_endpoint/mcp/?token=abc
```

After entering that, start the program:

```bash
python mcp_pipe.py calculator.py
```

### If you're using the control console deployment
If you're using the control console deployment, after starting, go back into the control console and click refresh on the MCP connection status; you'll see the list of extended features.

### If you're using a single-module deployment
If you're using a single-module deployment, after the device connects it will output logs like the following, indicating success:

```
250705 -INFO-Initializing MCP endpoint: wss://2662r3426b.vicp.fun/mcp_e
250705 -INFO-Sending MCP endpoint initialization message
250705 -INFO-MCP endpoint connected successfully
250705 -INFO-MCP endpoint initialized successfully
250705 -INFO-Unified tool handler initialization complete
250705 -INFO-MCP endpoint server info: name=Calculator, version=1.9.4
250705 -INFO-Number of tools supported by MCP endpoint: 1
250705 -INFO-All MCP endpoint tools retrieved, client is ready
250705 -INFO-Tool cache refreshed
250705 -INFO-Currently supported function list: [ 'get_time', 'get_lunar', 'play_music', 'get_weather', 'handle_exit_intent', 'calculator']
```
If `'calculator'` appears, the device can call the calculator tool through intent recognition.
