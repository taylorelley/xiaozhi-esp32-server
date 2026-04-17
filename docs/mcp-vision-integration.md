# Vision Model Usage Guide
This tutorial has two parts:
- Part 1: Enable the vision model when running xiaozhi-server in single-module mode
- Part 2: How to enable the vision model when running full-module mode

Before enabling the vision model, you need to prepare three things:
- You need a device with a camera, and that device already has the camera-calling feature implemented in Brother Xia's repository. For example, the `LCSC Hands-on ESP32-S3 development board`.
- The firmware version of your device has been upgraded to 1.6.6 or above.
- You have successfully run the basic conversation module.

## Enabling the vision model when running xiaozhi-server in single-module mode

### Step 1: Confirm networking
The vision model starts on port 8003 by default.

If you're running via docker, confirm whether your `docker-compose.yml` has exposed port `8003`. If not, update to the latest `docker-compose.yml` file.

If you're running from source, confirm that your firewall allows port `8003`.

### Step 2: Choose your vision model
Open your `data/.config.yaml` file and set `selected_module.VLLM` to a vision model. We currently support vision models with `openai`-type APIs. `ChatGLMVLLM` is one such `openai`-compatible model.

```
selected_module:
  VAD: ..
  ASR: ..
  LLM: ..
  VLLM: ChatGLMVLLM
  TTS: ..
  Memory: ..
  Intent: ..
```

Assuming we use `ChatGLMVLLM` as the vision model, we need to first log in to [Zhipu AI](https://bigmodel.cn/usercenter/proj-mgmt/apikeys) and apply for an API key. If you've already applied for a key, you can reuse it.

Add this config to your configuration file. If it already exists, just set your api_key.

```
VLLM:
  ChatGLMVLLM:
    api_key: your_api_key
```

### Step 3: Start the xiaozhi-server service
If you're running from source, enter the command to start it:
```
python app.py
```
If you're running via docker, restart the container:
```
docker restart xiaozhi-esp32-server
```

After startup, logs like the following will be output:

```
2025-06-01 **** - OTA interface:          http://192.168.4.7:8003/xiaozhi/ota/
2025-06-01 **** - Vision analysis API:    http://192.168.4.7:8003/mcp/vision/explain
2025-06-01 **** - Websocket address:      ws://192.168.4.7:8000/xiaozhi/v1/
2025-06-01 **** - =======The address above is a websocket protocol address; do NOT access it via browser=======
2025-06-01 **** - To test websocket, open test/test_page.html in Google Chrome
2025-06-01 **** - =============================================================
```

After startup, open the `Vision analysis API` link from the logs in a browser. What does it show? If you're on Linux without a browser, run:
```
curl -i your_vision_analysis_api
```

Normally it will display:
```
MCP Vision API is running normally. The vision explanation API address is: http://xxxx:8003/mcp/vision/explain
```

Note: if you're deploying over the public internet or via docker, you must update the following configuration in your `data/.config.yaml`:
```
server:
  vision_explain: http://your_ip_or_domain:port/mcp/vision/explain
```

Why? Because the vision explanation API must be pushed to the device. If your address is a LAN address or a Docker-internal address, the device won't be able to reach it.

Assuming your public IP is `111.111.111.111`, then `vision_explain` should be configured as:

```
server:
  vision_explain: http://111.111.111.111:8003/mcp/vision/explain
```

If your MCP Vision API is running normally and you can successfully open the pushed `vision explanation API address` in a browser, proceed to the next step.

### Step 4: Wake the device to enable

Say to the device: "Please turn on the camera and tell me what you see."

Watch the xiaozhi-server log output for any errors.


## How to enable the vision model when running full-module mode

### Step 1: Confirm networking
The vision model starts on port 8003 by default.

If you're running via docker, confirm that your `docker-compose_all.yml` has mapped port `8003`. If not, update to the latest `docker-compose_all.yml` file.

If you're running from source, confirm that your firewall allows port `8003`.

### Step 2: Confirm your config file

Open your `data/.config.yaml` file and confirm whether its structure matches `data/config_from_api.yaml`. If it doesn't, or something is missing, complete it.

### Step 3: Configure the vision model API key

We need to first log in to [Zhipu AI](https://bigmodel.cn/usercenter/proj-mgmt/apikeys) and apply for an API key. If you've applied for a key before, you can reuse it.

Log in to the `control console`, click `Model Configuration` in the top menu, and click `Vision LLM` in the left sidebar. Find `VLLM_ChatGLMVLLM`, click Modify. In the popup, enter your key into the `API key` field and click Save.

Once saved, go to the agent you want to test, click `Configure Role`. In the content that opens, check whether `Vision Large Language Model (VLLM)` is set to the vision model you just configured. Click Save.

### Step 3: Start the xiaozhi-server module
If you're running from source, enter the command to start it:
```
python app.py
```
If you're running via docker, restart the container:
```
docker restart xiaozhi-esp32-server
```

After startup, logs like the following will be output:

```
2025-06-01 **** - Vision analysis API:    http://192.168.4.7:8003/mcp/vision/explain
2025-06-01 **** - Websocket address:      ws://192.168.4.7:8000/xiaozhi/v1/
2025-06-01 **** - =======The address above is a websocket protocol address; do NOT access it via browser=======
2025-06-01 **** - To test websocket, open test/test_page.html in Google Chrome
2025-06-01 **** - =============================================================
```

After startup, open the `Vision analysis API` link from the logs in a browser. What does it show? If you're on Linux without a browser, run:
```
curl -i your_vision_analysis_api
```

Normally it will display:
```
MCP Vision API is running normally. The vision explanation API address is: http://xxxx:8003/mcp/vision/explain
```

Note: if you're deploying over the public internet or via docker, you must update the following configuration in your `data/.config.yaml`:
```
server:
  vision_explain: http://your_ip_or_domain:port/mcp/vision/explain
```

Why? Because the vision explanation API must be pushed to the device. If your address is a LAN address or a Docker-internal address, the device won't be able to reach it.

Assuming your public IP is `111.111.111.111`, then `vision_explain` should be configured as:

```
server:
  vision_explain: http://111.111.111.111:8003/mcp/vision/explain
```

If your MCP Vision API is running normally and you can successfully open the pushed `vision explanation API address` in a browser, proceed to the next step.

### Step 4: Wake the device to enable

Say to the device: "Please turn on the camera and tell me what you see."

Watch the xiaozhi-server log output for any errors.
