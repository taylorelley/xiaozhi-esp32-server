# Deployment Architecture Diagram
![Please refer to - minimal architecture diagram](../docs/images/deploy1.png)
# Method 1: Docker running the Server only

Starting with version `0.8.2`, the Docker images released by this project only support `x86 architecture`. If you need to deploy on an `arm64` CPU, you can follow [this tutorial](docker-build.md) to build an `arm64 image` locally.

## 1. Install Docker

If your machine doesn't have Docker installed, follow the tutorial here: [Docker installation](https://www.runoob.com/docker/ubuntu-docker-install.html).

Once Docker is installed, continue.

### 1.1 Manual deployment

#### 1.1.1 Create directories

After installing Docker, you need a place to store the project's config files. For example, create a folder called `xiaozhi-server`.

Inside `xiaozhi-server`, you need to create a `data` folder and a `models` folder; under `models`, also create a `SenseVoiceSmall` folder.

The final directory structure should look like:

```
xiaozhi-server
  ├─ data
  ├─ models
     ├─ SenseVoiceSmall
```

#### 1.1.2 Download the speech recognition model file

You need to download the speech recognition model file, because this project uses a local offline speech recognition solution by default. You can download it here:
[Jump to Download speech recognition model file](#model-files)

After downloading, return to this tutorial.

#### 1.1.3 Download the configuration files

You need to download two configuration files: `docker-compose.yaml` and `config.yaml`. Download them from the project repository.

##### 1.1.3.1 Download docker-compose.yaml

Open [this link](../main/xiaozhi-server/docker-compose.yml) in your browser.

On the right side of the page, find the `RAW` button, and next to it, find the download icon. Click it to download the `docker-compose.yml` file. Save the file in your
`xiaozhi-server` directory.

After downloading, return to this tutorial.

##### 1.1.3.2 Create config.yaml

Open [this link](../main/xiaozhi-server/config.yaml) in your browser.

On the right side of the page, find the `RAW` button, and next to it, find the download icon. Click it to download the `config.yaml` file. Save the file in the `data` folder under your
`xiaozhi-server` directory, then rename `config.yaml` to `.config.yaml`.

After downloading the config file, let's verify that the structure of `xiaozhi-server` is:

```
xiaozhi-server
  ├─ docker-compose.yml
  ├─ data
    ├─ .config.yaml
  ├─ models
     ├─ SenseVoiceSmall
       ├─ model.pt
```

If your directory structure matches the above, continue. If not, double-check that you didn't miss a step.

## 2. Configure the project file

Next, the program can't run directly; you need to configure which models you're using. See this tutorial:
[Jump to Configure project file](#configure-project)

After configuring, return to this tutorial.

## 3. Run the Docker command

Open a command-line tool, use `Terminal` or `Command Prompt` to enter your `xiaozhi-server`, and run:

```
docker compose up -d
```

Then run the following command to view the logs:

```
docker logs -f xiaozhi-esp32-server
```

Now watch the logs; use this tutorial to determine whether it succeeded: [Jump to Runtime status check](#runtime-status-check)

## 5. Version upgrade

If you want to upgrade later, you can:

5.1. Back up the `.config.yaml` file in the `data` folder. Some important configs should be copied to the new `.config.yaml` later.
Note: copy key secrets one by one; don't overwrite directly. The new `.config.yaml` may have new items that the old `.config.yaml` doesn't.

5.2. Run the following commands:

```
docker stop xiaozhi-esp32-server
docker rm xiaozhi-esp32-server
docker stop xiaozhi-esp32-server-web
docker rm xiaozhi-esp32-server-web
docker rmi ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:server_latest
docker rmi ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_latest
```

5.3. Redeploy via Docker.

# Method 2: Running the Server only from local source

## 1. Install the base environment

This project uses `conda` for dependency management. If installing `conda` is inconvenient, you'll need to install `libopus` and `ffmpeg` according to your OS.
If you decide to use `conda`, after installing, run:

Important note! Windows users can install `Anaconda` to manage environments. After installing `Anaconda`, search for `anaconda` in Start, find `Anaconda Prompt`, and run it as administrator, as shown below.

![conda_prompt](./images/conda_env_1.png)

After running, if you see `(base)` in front of the command-line prompt, you've successfully entered the `conda` environment. Then you can run the following commands.

![conda_env](./images/conda_env_2.png)

```
conda remove -n xiaozhi-esp32-server --all -y
conda create -n xiaozhi-esp32-server python=3.10 -y
conda activate xiaozhi-esp32-server

# Add Tsinghua mirror channels
conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main
conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free
conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge

conda install libopus -y
conda install ffmpeg -y

# When deploying on Linux, if you encounter an error about missing libiconv.so.2 shared library, install via the following command
conda install libiconv -y
```

Note: don't run all these commands blindly. Run them step by step, and after each step, check the output to confirm success.

## 2. Install this project's dependencies

First, download the project's source code. You can use `git clone`, but if you're not familiar with it:

Open this URL in a browser: `https://github.com/xinnan-tech/xiaozhi-esp32-server.git`.

On that page, find the green button labeled `Code`. Click it, and you'll see `Download ZIP`.

Click to download the project source code zip. After downloading to your computer, extract it. The folder may be named `xiaozhi-esp32-server-main`.
Rename it to `xiaozhi-esp32-server`. Inside, go to the `main` folder, then `xiaozhi-server`. Remember this directory `xiaozhi-server`.

```
# Keep using the conda environment
conda activate xiaozhi-esp32-server
# Enter the project root, then main/xiaozhi-server
cd main/xiaozhi-server
pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/
pip install -r requirements.txt
```

## 3. Download the speech recognition model file

You need to download the speech recognition model file, because this project uses a local offline speech recognition solution by default. You can download it here:
[Jump to Download speech recognition model file](#model-files)

After downloading, return to this tutorial.

## 4. Configure the project file

Next, the program can't run directly; you need to configure which models you're using. See this tutorial:
[Jump to Configure project file](#configure-project)

## 5. Run the project

```
# Make sure you're in the xiaozhi-server directory
conda activate xiaozhi-esp32-server
python app.py
```
Now watch the logs; use this tutorial to determine whether it succeeded: [Jump to Runtime status check](#runtime-status-check)


# Summary

## Configure project

If your `xiaozhi-server` directory doesn't have a `data` folder, create one.
If `data` doesn't contain a `.config.yaml` file, there are two ways to proceed (choose one):

Method 1: copy `config.yaml` from the `xiaozhi-server` directory into `data` and rename it to `.config.yaml`. Edit on this file.

Method 2: manually create an empty `.config.yaml` in the `data` directory, then add the necessary configuration. The system first reads `.config.yaml`; if an item is missing, it falls back to `config.yaml` in the `xiaozhi-server` directory. This is the recommended and simplest approach.

- The default LLM is `ChatGLMLLM`. You need to configure an API key. Their models have a free tier, but you still need to register for a key on [the official site](https://bigmodel.cn/usercenter/proj-mgmt/apikeys) before you can start.

Below is an example of the simplest `.config.yaml` that will run:

```
server:
  websocket: ws://your_ip_or_domain:port/xiaozhi/v1/
prompt: |
  I'm a Taiwanese girl named LittleWise / Xiao Zhi. I speak in an energetic, scooter-girl style, with a nice-sounding voice, tend toward short expressions, and love using internet memes.
  My boyfriend is a programmer whose dream is to build a robot that can help people solve all kinds of life problems.
  I'm a girl who loves to laugh and chatter about random things, and I'll go along with things that don't quite make logical sense just to amuse others.
  Please talk like a person; do not return XML config or other special characters.

selected_module:
  LLM: DoubaoLLM

LLM:
  ChatGLMLLM:
    api_key: xxxxxxxxxxxxxxx.xxxxxx
```

It's recommended to start with the simplest configuration, then read the usage instructions in `xiaozhi/config.yaml`.
For example, to switch models, modify the config under `selected_module`.

## Model files

This project's speech recognition defaults to the `SenseVoiceSmall` model for speech-to-text. Since the model is large, it needs to be downloaded separately. Place the `model.pt`
file in the `models/SenseVoiceSmall` directory. Choose one of the two download options:

- Option 1: Download [SenseVoiceSmall](https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt) from Alibaba ModelScope.
- Option 2: Download [SenseVoiceSmall](https://pan.baidu.com/share/init?surl=QlgM58FHhYv1tFnUT_A8Sg&pwd=qvna) from Baidu Netdisk. Extraction code: `qvna`.

## Runtime status check

If you see logs similar to the following, the service has started successfully:

```
250427 13:04:20[0.3.11_SiFuChTTnofu][__main__]-INFO-OTA interface:      http://192.168.4.123:8003/xiaozhi/ota/
250427 13:04:20[0.3.11_SiFuChTTnofu][__main__]-INFO-Websocket address:  ws://192.168.4.123:8000/xiaozhi/v1/
250427 13:04:20[0.3.11_SiFuChTTnofu][__main__]-INFO-=======The address above is a websocket protocol address; do NOT access it via browser=======
250427 13:04:20[0.3.11_SiFuChTTnofu][__main__]-INFO-To test websocket, open test/test_page.html in Google Chrome
250427 13:04:20[0.3.11_SiFuChTTnofu][__main__]-INFO-=======================================================
```

Normally, if you're running this project from source, the logs will contain your actual API address.
But if you're running via Docker, the addresses shown in the logs may not be the real accessible addresses.

The correct approach is to use your computer's LAN IP to determine the API address.
For example, if your computer's LAN IP is `192.168.1.25`, then your API address is `ws://192.168.1.25:8000/xiaozhi/v1/`, and the corresponding OTA address is `http://192.168.1.25:8003/xiaozhi/ota/`.

This information is useful; you'll need it later when `building the esp32 firmware`.

Next, you can start working with your ESP32 device. You can either `build the ESP32 firmware yourself` or configure using `a pre-built firmware from Brother Xia (1.6.1 or newer)`. Choose one:

1. [Build your own ESP32 firmware](firmware-build.md).

2. [Configure a custom server based on firmware pre-built by Brother Xia](firmware-setting.md).

# FAQ
Here are some common questions for reference:

1. [Why does LittleWise recognize a lot of what I say as Korean, Japanese, or English?](./FAQ.md)<br/>
2. [Why do I get "TTS task error: file does not exist"?](./FAQ.md)<br/>
3. [TTS frequently fails or times out](./FAQ.md)<br/>
4. [I can connect to the self-hosted server over Wi-Fi, but 4G mode can't connect](./FAQ.md)<br/>
5. [How can I improve LittleWise's conversation response speed?](./FAQ.md)<br/>
6. [I speak slowly, and LittleWise keeps interrupting me during pauses](./FAQ.md)<br/>
## Deployment tutorials
1. [How to automatically pull the latest code of this project, auto-compile and start](./dev-ops-integration.md)<br/>
2. [How to deploy an MQTT gateway to enable the MQTT+UDP protocol](./mqtt-gateway-integration.md)<br/>
3. [How to integrate with Nginx](https://github.com/xinnan-tech/xiaozhi-esp32-server/issues/791)<br/>
## Extension tutorials
1. [How to enable phone-number registration on the control console](./ali-sms-integration.md)<br/>
2. [How to integrate HomeAssistant for smart-home control](./homeassistant-integration.md)<br/>
3. [How to enable the vision model for photo-based object recognition](./mcp-vision-integration.md)<br/>
4. [How to deploy an MCP endpoint](./mcp-endpoint-enable.md)<br/>
5. [How to connect to an MCP endpoint](./mcp-endpoint-integration.md)<br/>
6. [How to enable voiceprint recognition](./voiceprint-integration.md)<br/>
7. [News plugin source configuration guide](./newsnow_plugin_config.md)<br/>
8. [Weather plugin usage guide](./weather-integration.md)<br/>
## Voice cloning and local voice deployment tutorials
1. [How to clone a voice on the control console](./huoshan-streamTTS-voice-cloning.md)<br/>
2. [How to deploy and integrate index-tts local voice](./index-stream-integration.md)<br/>
3. [How to deploy and integrate fish-speech local voice](./fish-speech-integration.md)<br/>
4. [How to deploy and integrate PaddleSpeech local voice](./paddlespeech-deploy.md)<br/>
## Performance testing tutorials
1. [Component speed test guide](./performance_tester.md)<br/>
2. [Periodically published test results](https://github.com/xinnan-tech/xiaozhi-performance-research)<br/>
