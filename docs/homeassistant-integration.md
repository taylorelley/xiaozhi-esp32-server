# LittleWise ESP32 Open-Source Server Integration Guide with HomeAssistant

[TOC]

-----

## Introduction

This document walks you through integrating ESP32 devices with HomeAssistant.

## Prerequisites

- `HomeAssistant` is installed and configured.
- For this guide, I'm using the free ChatGLM model, which supports function calls.

## Steps before starting (required)

### 1. Obtain HomeAssistant's network address

Visit your Home Assistant's network address. For example, my HA address is 192.168.4.7, the port is the default 8123, so open in a browser:

```
http://192.168.4.7:8123
```

> How to manually look up the HA IP address **(only when LittleWise esp32-server and HA are on the same network device, e.g., the same Wi-Fi)**:
>
> 1. Open Home Assistant (frontend).
>
> 2. Click the bottom-left **Settings** -> **System** -> **Network**.
>
> 3. Scroll to the bottom `Home Assistant website` section. Under `local network`, click the `eye` button to reveal the current IP (such as `192.168.1.10`) and the network interface. Click `copy link` to copy it directly.
>
>    ![image-20250504051716417](images/image-ha-integration-01.png)

Alternatively, if you've set an OAuth address for direct access to Home Assistant, you can also visit directly:

```
http://homeassistant.local:8123
```

### 2. Log in to `Home Assistant` to obtain a developer key

Log in to `HomeAssistant`, click `your avatar at the bottom-left -> Profile`, switch to the `Security` tab, scroll to the bottom to `Long-Lived Access Tokens`, generate an api_key, copy and save it. This api key is required for later and is shown only once (tip: you can save the generated QR code image and scan it later to retrieve the api key).

## Method 1: LittleWise community-built HA call feature

### Feature description

- If you add new devices later, this method requires you to manually restart the `xiaozhi-esp32-server` to refresh the device info **(important)**.

- You need to make sure you've integrated `Xiaomi Home` in HomeAssistant and imported Mi Home devices into `HomeAssistant`.

- You need to make sure your `xiaozhi-esp32-server control console` is working properly.

- My `xiaozhi-esp32-server control console` and `HomeAssistant` are deployed on the same machine on different ports; version is `0.3.10`.

  ```
  http://192.168.4.7:8002
  ```


### Configuration steps

#### 1. Log in to `HomeAssistant` and list the devices you want to control

Log in to `HomeAssistant`, click `Settings` at the bottom-left, then go to `Devices & Services`, and click `Entities` at the top.

Search for the switch you want to control. From the results, click one result and a switch panel will appear.

On the switch panel, try clicking the switch and see whether it actually toggles the device on/off. If it works, the network connection is fine.

Then find the Settings button on the switch panel. After clicking, you can view the `entity identifier` of this switch.

Open a notes app and format one entry per device as follows:

location + English comma + device name + English comma + `entity identifier` + English semicolon

For example, I'm at the office; I have a toy lamp whose identifier is switch.cuco_cn_460494544_cp1_on_p_2_1, so I write one entry:

```
Office,Toy lamp,switch.cuco_cn_460494544_cp1_on_p_2_1;
```

If I also want to control a second lamp, my final result is:

```
Office,Toy lamp,switch.cuco_cn_460494544_cp1_on_p_2_1;
Office,Desk lamp,switch.iot_cn_831898993_socn1_on_p_2_1;
```

We call this string the "device list string". Keep it; it'll be used shortly.

#### 2. Log in to the `control console`

![image-20250504051716417](images/image-ha-integration-06.png)

Log in to the `control console` as an administrator. Under `Agent Management`, find your agent and click `Configure Role`.

Set intent recognition to `External LLM Intent Recognition` or `LLM Autonomous Function Call`. You'll see an `Edit Features` button on the right. Click `Edit Features`, and a `Feature Management` dialog will pop up.

In the `Feature Management` dialog, check `HomeAssistant device state query` and `HomeAssistant device state modification`.

After checking, click `HomeAssistant device state query` in `Selected Features`, then in `Parameter Configuration`, configure your `HomeAssistant` address, key, and device list string.

After editing, click `Save Configuration`. The `Feature Management` dialog will close. Then click Save on the agent configuration.

Once saved, wake the device and give commands.

#### 3. Wake the device to control

Try saying to the ESP32: "Turn on the XXX lamp".

## Method 2: Use Home Assistant's voice assistant as the LLM tool in LittleWise

### Feature description

- This method has a notable downside: **you cannot use the function_call plugin capabilities of the LittleWise open-source ecosystem**, because using Home Assistant as LittleWise's LLM tool delegates intent recognition to Home Assistant. But **you do get the native Home Assistant control experience, and LittleWise's chat capabilities are retained**. If this bothers you, use the also Home-Assistant-supported [Method 3](##method-3-use-home-assistants-mcp-service-recommended), which lets you experience Home Assistant's features to the fullest.

### Configuration steps:

#### 1. Configure Home Assistant's LLM voice assistant.

**You must configure Home Assistant's voice assistant or LLM tool in advance.**

#### 2. Obtain the Agent ID of the Home Assistant voice assistant.

1. Go to the Home Assistant page. On the left, click `Developer Tools`.
2. In the opened `Developer Tools`, click the `Actions` tab (as shown in operation 1). On the page, in the `Action` field, find or type `conversation.process` and select `Conversation: Process` (as shown in operation 2).

![image-20250504043539343](images/image-ha-integration-02.png)

3. On the page, check the `agent` option. In the now highlighted `conversation agent`, select the voice assistant name you configured in Step 1. In the image, mine is `ZhipuAi`.

![image-20250504043854760](images/image-ha-integration-03.png)

4. After selection, click `Go to YAML mode` at the bottom-left of the form.

![image-20250504043951126](images/image-ha-integration-04.png)

5. Copy the value of `agent-id`. For example, in my image it is `01JP2DYMBDF7F4ZA2DMCF2AGX2` (for reference).

![image-20250504044046466](images/image-ha-integration-05.png)

6. Switch to the `config.yaml` file of the LittleWise open-source server `xiaozhi-esp32-server`. In the LLM configuration, find `Home Assistant` and set your Home Assistant's network address, API key, and the agent_id you just found.
7. In `config.yaml`, set `selected_module.LLM` to `HomeAssistant` and `Intent` to `nointent`.
8. Restart the LittleWise open-source server `xiaozhi-esp32-server` and you're good to go.

## Method 3: Use Home Assistant's MCP service (recommended)

### Feature description

- You must integrate and install the HA integration [Model Context Protocol Server](https://www.home-assistant.io/integrations/mcp_server/) inside Home Assistant in advance.

- Both this method and Method 2 are HA-official solutions. Unlike Method 2, you can still use the community-built plugins of the LittleWise open-source server `xiaozhi-esp32-server`, and you can freely use any LLM that supports function_call.

### Configuration steps

#### 1. Install the Home Assistant MCP service integration.

Official integration page: [Model Context Protocol Server](https://www.home-assistant.io/integrations/mcp_server/).

Or follow the manual steps below.

> - Go to **[Settings > Devices & Services](https://my.home-assistant.io/redirect/integrations)** in Home Assistant.
>
> - In the lower-right, click **[Add Integration](https://my.home-assistant.io/redirect/config_flow_start?domain=mcp_server)**.
>
> - From the list, select **Model Context Protocol Server**.
>
> - Follow the on-screen instructions to complete setup.

#### 2. Configure the LittleWise open-source server MCP config


Enter the `data` directory and find `.mcp_server_settings.json`.

If your `data` directory doesn't have `.mcp_server_settings.json`:
- Copy `mcp_server_settings.json` from the `xiaozhi-server` folder root into the `data` directory and rename it to `.mcp_server_settings.json`
- Or [download this file](https://github.com/xinnan-tech/xiaozhi-esp32-server/blob/main/main/xiaozhi-server/mcp_server_settings.json) into the `data` directory and rename it to `.mcp_server_settings.json`


Modify the following portion of `"mcpServers"`:

```json
"Home Assistant": {
      "command": "mcp-proxy",
      "args": [
        "http://YOUR_HA_HOST/mcp_server/sse"
      ],
      "env": {
        "API_ACCESS_TOKEN": "YOUR_API_ACCESS_TOKEN"
      }
},
```

Notes:

1. **Replace values:**
   - Replace `YOUR_HA_HOST` in `args` with your HA service address. If the URL already includes https/http (e.g., `http://192.168.1.101:8123`), just fill in `192.168.1.101:8123`.
   - Replace `YOUR_API_ACCESS_TOKEN` in `env.API_ACCESS_TOKEN` with the dev API key you obtained earlier.
2. **If the only entry you added is inside the `"mcpServers"` braces with no further `mcpServers` entries, remove the trailing comma `,`**, otherwise parsing may fail.

**Example of final result (reference)**:

```json
 "mcpServers": {
    "Home Assistant": {
      "command": "mcp-proxy",
      "args": [
        "http://192.168.1.101:8123/mcp_server/sse"
      ],
      "env": {
        "API_ACCESS_TOKEN": "abcd.efghi.jkl"
      }
    }
  }
```

#### 3. Configure LittleWise open-source server system settings

1. **Choose any LLM that supports function_call as the LittleWise chat assistant (but don't choose Home Assistant as the LLM tool)**. In this guide I chose the free ChatGLM, which supports function_call, though it can sometimes be unstable. If you want stability, I recommend setting the LLM to DoubaoLLM, with model_name `doubao-1-5-pro-32k-250115`.

2. Switch to `config.yaml` in `xiaozhi-esp32-server`. Set up your LLM config, and in `selected_module` set `Intent` to `function_call`.

3. Restart `xiaozhi-esp32-server` to use it.
