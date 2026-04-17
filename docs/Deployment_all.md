# Deployment Architecture Diagram
![Please refer to - full-module install architecture diagram](../docs/images/deploy2.png)
# Method 1: Run all modules via Docker
Starting with version `0.8.2`, the Docker images released by this project only support `x86 architecture`. If you need to deploy on an `arm64` CPU, you can follow [this tutorial](docker-build.md) to build an `arm64` image locally.

## 1. Install Docker

If your machine doesn't have Docker installed, follow the tutorial here: [Docker installation](https://www.runoob.com/docker/ubuntu-docker-install.html).

There are two ways to install the full-module Docker deployment. You can [use the lazy script](./Deployment_all.md#11-lazy-script) (author [@VanillaNahida](https://github.com/VanillaNahida)),
which will automatically download the required files and configuration, or you can use the [manual deployment](./Deployment_all.md#12-manual-deployment) to set everything up from scratch.



### 1.1 Lazy script
Deployment is very simple. You can refer to this [video tutorial](https://www.bilibili.com/video/BV17bbvzHExd/). The text version is below:
> [!NOTE]
> Currently only Ubuntu one-click deployment is supported; other systems haven't been tested and may have odd bugs.

Use an SSH tool to connect to the server and run the following script as root:
```bash
sudo bash -c "$(wget -qO- https://ghfast.top/https://raw.githubusercontent.com/xinnan-tech/xiaozhi-esp32-server/main/docker-setup.sh)"
```

The script automatically does the following:
> 1. Install Docker
> 2. Configure the mirror source
> 3. Download/pull images
> 4. Download the speech recognition model file
> 5. Guide you through the server configuration
>

After execution, after a simple configuration, refer to the three most important steps mentioned in [4. Run the program](#4-run-the-program) and [5. Restart xiaozhi-esp32-server](#5-restart-xiaozhi-esp32-server). Once those three steps are complete, it's ready to use.

### 1.2 Manual deployment

#### 1.2.1 Create directories

After installing Docker, you need a place to store the project's config files. For example, create a folder called `xiaozhi-server`.

Inside `xiaozhi-server`, create a `data` folder and a `models` folder; under `models`, also create a `SenseVoiceSmall` folder.

The final directory structure should look like:

```
xiaozhi-server
  ├─ data
  ├─ models
     ├─ SenseVoiceSmall
```

#### 1.2.2 Download the speech recognition model file

This project's speech recognition defaults to the `SenseVoiceSmall` model for speech-to-text. Since the model is large, it needs to be downloaded separately. Place the `model.pt`
file in the `models/SenseVoiceSmall` directory. Choose one of the two download options:

- Option 1: Download [SenseVoiceSmall](https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt) from Alibaba ModelScope.
- Option 2: Download [SenseVoiceSmall](https://pan.baidu.com/share/init?surl=QlgM58FHhYv1tFnUT_A8Sg&pwd=qvna) from Baidu Netdisk. Extraction code: `qvna`.


#### 1.2.3 Download configuration files

You need to download two configuration files: `docker-compose_all.yaml` and `config_from_api.yaml`. Download them from the project repository.

##### 1.2.3.1 Download docker-compose_all.yaml

Open [this link](../main/xiaozhi-server/docker-compose_all.yml) in your browser.

On the right of the page, find the `RAW` button, and next to it, find the download icon. Click it to download `docker-compose_all.yml`. Save the file in your
`xiaozhi-server` directory.

Or just run `wget https://raw.githubusercontent.com/xinnan-tech/xiaozhi-esp32-server/refs/heads/main/main/xiaozhi-server/docker-compose_all.yml` to download it.

After downloading, return to this tutorial.

##### 1.2.3.2 Download config_from_api.yaml

Open [this link](../main/xiaozhi-server/config_from_api.yaml) in your browser.

On the right of the page, find the `RAW` button, and next to it, find the download icon. Click it to download `config_from_api.yaml`. Save the file in the `data` folder under your
`xiaozhi-server` directory, then rename `config_from_api.yaml` to `.config.yaml`.

Or just run `wget https://raw.githubusercontent.com/xinnan-tech/xiaozhi-esp32-server/refs/heads/main/main/xiaozhi-server/config_from_api.yaml` to download and save it.

After downloading the config file, let's verify that the whole `xiaozhi-server` structure is:

```
xiaozhi-server
  ├─ docker-compose_all.yml
  ├─ data
    ├─ .config.yaml
  ├─ models
     ├─ SenseVoiceSmall
       ├─ model.pt
```

If your directory structure matches, continue. If not, double-check that you didn't miss a step.

## 2. Back up data

If you've already successfully run the control console and you have secret information stored there, please copy important data from the control console first. The upgrade may overwrite existing data.

## 3. Clear old image versions and containers
Open a command-line tool, use `Terminal` or `Command Prompt` to enter your `xiaozhi-server`, and run:

```
docker compose -f docker-compose_all.yml down

docker stop xiaozhi-esp32-server
docker rm xiaozhi-esp32-server

docker stop xiaozhi-esp32-server-web
docker rm xiaozhi-esp32-server-web

docker stop xiaozhi-esp32-server-db
docker rm xiaozhi-esp32-server-db

docker stop xiaozhi-esp32-server-redis
docker rm xiaozhi-esp32-server-redis

docker rmi ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:server_latest
docker rmi ghcr.nju.edu.cn/xinnan-tech/xiaozhi-esp32-server:web_latest
```

## 4. Run the program
Run the following to start the new-version containers:

```
docker compose -f docker-compose_all.yml up -d
```

Then run the following to view the logs:

```
docker logs -f xiaozhi-esp32-server-web
```

When you see output like the following, your `control console` has started successfully:

```
2025-xx-xx 22:11:12.445 [main] INFO  c.a.d.s.b.a.DruidDataSourceAutoConfigure - Init DruidDataSource
2025-xx-xx 21:28:53.873 [main] INFO  xiaozhi.AdminApplication - Started AdminApplication in 16.057 seconds (process running for 17.941)
http://localhost:8002/xiaozhi/doc.html
```

Note: only the `control console` is running now. If port 8000 `xiaozhi-esp32-server` throws errors, ignore them for now.

Now open the `control console` in a browser: http://127.0.0.1:8002, and register the first user. The first user is the super administrator; subsequent users are ordinary users. Ordinary users can only bind devices and configure agents; the super administrator can manage models, users, and parameters.

Three important things to do next:

### First important thing

Log in to the control console as super administrator. In the top menu, find `Parameter Management`. Find the first item in the list whose parameter code is `server.secret` and copy its `Parameter Value`.

About `server.secret`: this `Parameter Value` is very important. It allows our `Server` side to connect to `manager-api`. `server.secret` is a random key automatically generated each time you deploy the manager module from scratch.

After copying the `Parameter Value`, open the file `.config.yaml` in the `data` folder under `xiaozhi-server`. Your config file should look like:

```
manager-api:
  url:  http://127.0.0.1:8002/xiaozhi
  secret: your server.secret value
```
1. Paste the `server.secret` `Parameter Value` you just copied from the `control console` into the `secret` field in `.config.yaml`.

2. Because this is a docker deployment, change `url` to `http://xiaozhi-esp32-server-web:8002/xiaozhi`.

3. Because this is a docker deployment, change `url` to `http://xiaozhi-esp32-server-web:8002/xiaozhi`.

4. Because this is a docker deployment, change `url` to `http://xiaozhi-esp32-server-web:8002/xiaozhi`.

Something like:
```
manager-api:
  url: http://xiaozhi-esp32-server-web:8002/xiaozhi
  secret: 12345678-xxxx-xxxx-xxxx-123456789000
```

After saving, continue to the second important thing.

### Second important thing

Log in to the control console as super administrator. In the top menu, find `Model Configuration`, then in the left sidebar click `Large Language Model`. Find the first item `Zhipu AI`, click the `Modify` button.
In the popup, fill in the `Zhipu AI` API key you registered for in the `API key` field. Then click save.

## 5. Restart xiaozhi-esp32-server

Open a command-line tool, use `Terminal` or `Command Prompt`, and run:
```
docker restart xiaozhi-esp32-server
docker logs -f xiaozhi-esp32-server
```
If you see logs like the following, the Server started successfully:

```
25-02-23 12:01:09[core.websocket_server] - INFO - Websocket address:  ws://xxx.xx.xx.xx:8000/xiaozhi/v1/
25-02-23 12:01:09[core.websocket_server] - INFO - =======The address above is a websocket protocol address; do NOT access it via browser=======
25-02-23 12:01:09[core.websocket_server] - INFO - To test websocket, open test/test_page.html in Google Chrome
25-02-23 12:01:09[core.websocket_server] - INFO - =======================================================
```

Since this is a full-module deployment, there are two important URLs that need to be written into the ESP32.

OTA URL:
```
http://your_host_LAN_ip:8002/xiaozhi/ota/
```

Websocket URL:
```
ws://your_host_ip:8000/xiaozhi/v1/
```

### Third important thing

Log in to the control console as super administrator. In the top menu, find `Parameter Management`, find the parameter code `server.websocket`, and enter your `Websocket URL`.

Log in to the control console as super administrator. In the top menu, find `Parameter Management`, find the parameter code `server.ota`, and enter your `OTA URL`.

Next, you can start working with your ESP32 device. You can either `build the ESP32 firmware yourself` or configure using `firmware pre-built by Brother Xia (1.6.1 or newer)`. Choose one:

1. [Build your own ESP32 firmware](firmware-build.md).

2. [Configure a custom server based on firmware pre-built by Brother Xia](firmware-setting.md).


# Method 2: Run all modules locally from source

## 1. Install a MySQL database

If MySQL is already installed on your machine, you can simply create a database named `xiaozhi_esp32_server`:

```sql
CREATE DATABASE xiaozhi_esp32_server CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

If MySQL is not installed, you can install it via Docker:

```
docker run --name xiaozhi-esp32-server-db -e MYSQL_ROOT_PASSWORD=123456 -p 3306:3306 -e MYSQL_DATABASE=xiaozhi_esp32_server -e MYSQL_INITDB_ARGS="--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci" -e TZ=Asia/Shanghai -d mysql:latest
```

## 2. Install Redis

If Redis is not installed, you can install it via Docker:

```
docker run --name xiaozhi-esp32-server-redis -d -p 6379:6379 redis
```

## 3. Run the manager-api program

3.1 Install JDK 21 and set the JDK environment variables.

3.2 Install Maven and set the Maven environment variables.

3.3 Using VSCode, install the Java-related extensions.

3.4 Using VSCode, load the manager-api module.

In `src/main/resources/application-dev.yml`, configure the database connection:

```
spring:
  datasource:
    username: root
    password: 123456
```
In `src/main/resources/application-dev.yml`, configure the Redis connection:
```
spring:
    data:
      redis:
        host: localhost
        port: 6379
        password:
        database: 0
```

3.5 Run the main program

This project is a Spring Boot project. To start:
Open `Application.java` and run the `Main` method.

```
Path:
src/main/java/xiaozhi/AdminApplication.java
```

When you see the logs below, your `manager-api` has started successfully:

```
2025-xx-xx 22:11:12.445 [main] INFO  c.a.d.s.b.a.DruidDataSourceAutoConfigure - Init DruidDataSource
2025-xx-xx 21:28:53.873 [main] INFO  xiaozhi.AdminApplication - Started AdminApplication in 16.057 seconds (process running for 17.941)
http://localhost:8002/xiaozhi/doc.html
```

## 4. Run the manager-web program

4.1 Install Node.js.

4.2 Using VSCode, load the manager-web module.

In the terminal, enter the manager-web directory:

```
npm install
```
Then start:
```
npm run serve
```

Note: if your manager-api is not at `http://localhost:8002`, modify the path in
`main/manager-web/.env.development` during development.

Once running successfully, open the `control console` in a browser: http://127.0.0.1:8001, and register the first user. The first user is the super administrator; subsequent users are ordinary users. Ordinary users can only bind devices and configure agents; the super administrator can manage models, users, and parameters.


Important: after successful registration, log in to the control console as super administrator, find `Model Configuration` in the top menu, click `Large Language Model` in the left sidebar, find `Zhipu AI`, click `Modify`, and in the popup, enter your `Zhipu AI` API key in the `API key` field. Then click save.

Important: after successful registration, log in to the control console as super administrator, find `Model Configuration` in the top menu, click `Large Language Model` in the left sidebar, find `Zhipu AI`, click `Modify`, and in the popup, enter your `Zhipu AI` API key in the `API key` field. Then click save.

Important: after successful registration, log in to the control console as super administrator, find `Model Configuration` in the top menu, click `Large Language Model` in the left sidebar, find `Zhipu AI`, click `Modify`, and in the popup, enter your `Zhipu AI` API key in the `API key` field. Then click save.

## 5. Install the Python environment

This project uses `conda` for dependency management. If installing `conda` is inconvenient, you'll need to install `libopus` and `ffmpeg` depending on your OS.
If you decide to use `conda`, after installing, run:

Important note! Windows users can install `Anaconda` to manage environments. After installing `Anaconda`, search for `anaconda` in Start, find `Anaconda Prompt`, and run it as administrator, as shown below.

![conda_prompt](./images/conda_env_1.png)

After running, if you see `(base)` in front of the command-line prompt, you've successfully entered the `conda` environment. You can then run the commands below.

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

Note: don't run all these commands blindly. Run them step by step and check the output at each step to confirm success.

## 6. Install this project's dependencies

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

### 7. Download the speech recognition model file

This project's speech recognition defaults to the `SenseVoiceSmall` model for speech-to-text. Since the model is large, it needs to be downloaded separately. Place the `model.pt`
file in the `models/SenseVoiceSmall` directory. Choose one of the two download options:

- Option 1: Download [SenseVoiceSmall](https://modelscope.cn/models/iic/SenseVoiceSmall/resolve/master/model.pt) from Alibaba ModelScope.
- Option 2: Download [SenseVoiceSmall](https://pan.baidu.com/share/init?surl=QlgM58FHhYv1tFnUT_A8Sg&pwd=qvna) from Baidu Netdisk. Extraction code: `qvna`.

## 8. Configure the project file

Log in to the control console as super administrator. In the top menu, find `Parameter Management`. Find the first item in the list whose parameter code is `server.secret`, and copy its `Parameter Value`.

About `server.secret`: this `Parameter Value` is very important. It allows our `Server` side to connect to `manager-api`. `server.secret` is a random key automatically generated each time you deploy the manager module from scratch.

If your `xiaozhi-server` directory doesn't have a `data` folder, create one.
If `data` doesn't contain a `.config.yaml` file, you can copy `config_from_api.yaml` from the `xiaozhi-server` directory into `data` and rename it to `.config.yaml`.

After copying the `Parameter Value`, open the file `.config.yaml` in the `data` folder under `xiaozhi-server`. Your config should look like:

```
manager-api:
  url: http://127.0.0.1:8002/xiaozhi
  secret: your server.secret value
```

Paste the `server.secret` `Parameter Value` you copied from the `control console` into the `secret` field in `.config.yaml`.

Something like:
```
manager-api:
  url: http://127.0.0.1:8002/xiaozhi
  secret: 12345678-xxxx-xxxx-xxxx-123456789000
```

## 5. Run the project

```
# Make sure you're in the xiaozhi-server directory
conda activate xiaozhi-esp32-server
python app.py
```

If you see logs like the following, the service has started successfully:

```
25-02-23 12:01:09[core.websocket_server] - INFO - Server is running at ws://xxx.xx.xx.xx:8000/xiaozhi/v1/
25-02-23 12:01:09[core.websocket_server] - INFO - =======The address above is a websocket protocol address; do NOT access it via browser=======
25-02-23 12:01:09[core.websocket_server] - INFO - To test websocket, open test/test_page.html in Google Chrome
25-02-23 12:01:09[core.websocket_server] - INFO - =======================================================
```

Since this is a full-module deployment, you have two important URLs.

OTA URL:
```
http://your_computer_LAN_ip:8002/xiaozhi/ota/
```

Websocket URL:
```
ws://your_computer_LAN_ip:8000/xiaozhi/v1/
```

Please write these two URLs into the control console: they affect the websocket address broadcasting and auto-upgrade features.

1. Log in to the control console as super administrator. In the top menu, find `Parameter Management`, find the parameter code `server.websocket`, and enter your `Websocket URL`.

2. Log in to the control console as super administrator. In the top menu, find `Parameter Management`, find the parameter code `server.ota`, and enter your `OTA URL`.


Next, you can start working with your ESP32 device. You can either `build the ESP32 firmware yourself` or configure using `firmware pre-built by Brother Xia (1.6.1 or newer)`. Choose one:

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
