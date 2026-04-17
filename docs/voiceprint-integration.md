# Voiceprint Recognition Guide

This tutorial has three parts:
- 1. How to deploy the voiceprint recognition service
- 2. How to configure the voiceprint recognition API for a full-module deployment
- 3. How to configure voiceprint recognition for a minimal deployment

# 1. How to deploy the voiceprint recognition service

## Step 1: Download the voiceprint recognition project source code

Open the [voiceprint recognition project page](https://github.com/xinnan-tech/voiceprint-api) in your browser.

On the page, find the green button labeled `Code`. Click it and you'll see a `Download ZIP` button.

Click to download the project source code zip. After extracting, the folder may be named `voiceprint-api-main`.
Rename it to `voiceprint-api`.

## Step 2: Create the database and table

Voiceprint recognition depends on a `mysql` database. If you previously deployed the `control console`, you already have `mysql` installed and can share it.

You can try using the `telnet` command on the host to see whether you can reach port `3306` of `mysql`:
```
telnet 127.0.0.1 3306
```
If you can access port 3306, skip the rest of this step and go directly to Step 3.

If you can't access it, think about how your `mysql` was installed.

If you installed mysql yourself with an installer, your `mysql` is network-isolated. You may need to resolve the issue of accessing port `3306` first.

If your `mysql` was installed via this project's `docker-compose_all.yml`, you need to find the `docker-compose_all.yml` file you used to create the database and modify:

Before:
```
  xiaozhi-esp32-server-db:
    ...
    networks:
      - default
    expose:
      - "3306:3306"
```

After:
```
  xiaozhi-esp32-server-db:
    ...
    networks:
      - default
    ports:
      - "3306:3306"
```

Note: change the `expose` under `xiaozhi-esp32-server-db` to `ports`. After changing, restart. Here are the commands to restart mysql:

```
# Enter the folder containing your docker-compose_all.yml. For example, mine is xiaozhi-server.
cd xiaozhi-server
docker compose -f docker-compose_all.yml down
docker compose -f docker-compose.yml up -d
```

After restarting, use `telnet` on the host again to check port `3306` of `mysql`:
```
telnet 127.0.0.1 3306
```
Normally this will now be accessible.

## Step 3: Create the database and table
If your host can access the mysql database normally, then create a database named `voiceprint_db` and a `voiceprints` table:

```
CREATE DATABASE voiceprint_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE voiceprint_db;

CREATE TABLE voiceprints (
    id INT AUTO_INCREMENT PRIMARY KEY,
    speaker_id VARCHAR(255) NOT NULL UNIQUE,
    feature_vector LONGBLOB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_speaker_id (speaker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Step 4: Configure the database connection

Enter the `voiceprint-api` folder and create a folder named `data`.

Copy `voiceprint.yaml` from the `voiceprint-api` root directory into the `data` folder and rename it to `.voiceprint.yaml`.

Next, you need to configure the database connection in `.voiceprint.yaml`:

```
mysql:
  host: "127.0.0.1"
  port: 3306
  user: "root"
  password: "your_password"
  database: "voiceprint_db"
```

Note! Since your voiceprint recognition service is deployed via docker, `host` should be the `LAN IP of the machine where mysql runs`.

Note! Since your voiceprint recognition service is deployed via docker, `host` should be the `LAN IP of the machine where mysql runs`.

Note! Since your voiceprint recognition service is deployed via docker, `host` should be the `LAN IP of the machine where mysql runs`.

## Step 5: Start the program
This project is very simple and is recommended to run via docker. If you don't want to use docker, see [this page](https://github.com/xinnan-tech/voiceprint-api/blob/main/README.md) to run from source. The docker method is:

```
# Enter the project source code root directory
cd voiceprint-api

# Clean up caches
docker compose -f docker-compose.yml down
docker stop voiceprint-api
docker rm voiceprint-api
docker rmi ghcr.nju.edu.cn/xinnan-tech/voiceprint-api:latest

# Start the docker container
docker compose -f docker-compose.yml up -d
# View the logs
docker logs -f voiceprint-api
```

The logs will output something like:
```
250711 INFO-🚀 Starting: production service (Uvicorn), listening on: 0.0.0.0:8005
250711 INFO-============================================================
250711 INFO-Voiceprint API address: http://127.0.0.1:8005/voiceprint/health?key=abcd
250711 INFO-============================================================
```

Please copy the voiceprint API address:

Since you're using a docker deployment, do NOT use the above address directly!

Since you're using a docker deployment, do NOT use the above address directly!

Since you're using a docker deployment, do NOT use the above address directly!

First copy the address to a draft, find out your computer's LAN IP. For example, my computer's LAN IP is `192.168.1.25`, so
my original address
```
http://127.0.0.1:8005/voiceprint/health?key=abcd

```
needs to be changed to
```
http://192.168.1.25:8005/voiceprint/health?key=abcd
```

After changing, visit the `Voiceprint API address` directly in your browser. If the browser displays something like:
```
{"total_voiceprints":0,"status":"healthy"}
```
it's working.

Keep the updated `Voiceprint API address`; you'll need it in the next step.

# 2. How to configure voiceprint recognition for a full-module deployment

## Step 1: Configure the API
First, enable the voiceprint recognition feature. In the control console, click `Parameter Dictionary` at the top, then click `System Feature Configuration` in the dropdown. On the page, check `Voiceprint Recognition`, then click `Save Configuration`. You'll then see a `Voiceprint Recognition` button on the agent card when creating agents.

If you're using a full-module deployment, log in to the control console with an admin account, click `Parameter Dictionary` at the top, and select `Parameter Management`.

Search for the parameter `server.voice_print`. Its value should be `null`.
Click Edit, paste the `Voiceprint API address` from the previous step into the `Parameter Value` field, and save.

If the save succeeds, everything is fine; you can go to the agent to verify. If it fails, it means the control console cannot reach the voiceprint recognition service, most likely due to a network firewall or an incorrect LAN IP.

## Step 2: Set the agent's memory mode

Go to your agent's role configuration. Set memory to `local short-term memory` and make sure `Report text + voice` is enabled.

## Step 3: Chat with your agent

Power on your device and chat with it at a normal speed and tone.

## Step 4: Register the voiceprint

In the control console, on the `Agent Management` page, each agent card has a `Voiceprint Recognition` button. Click it. At the bottom there's an `Add` button to register a speaker's voiceprint.
In the popup, it's recommended to fill in the `Description` with the person's occupation, personality, or hobbies, so the agent can better analyze and understand the speaker.

## Step 3: Chat with your agent

Power on the device and ask: "Do you know who I am?" If it can answer, voiceprint recognition is working.

# 3. How to configure voiceprint recognition for a minimal deployment

## Step 1: Configure the API
Open `xiaozhi-server/data/.config.yaml` (create it if it doesn't exist), then add/modify:

```
# Voiceprint recognition config
voiceprint:
  # Voiceprint API address
  url: your voiceprint API address
  # Speaker config: speaker_id,name,description
  speakers:
    - "test1,Zhang San,Zhang San is a programmer"
    - "test2,Li Si,Li Si is a product manager"
    - "test3,Wang Wu,Wang Wu is a designer"
```

Paste the `Voiceprint API address` from the previous step into `url`, then save.

Add `speakers` as needed. Note that the `speaker_id` parameter will be used when registering voiceprints.

## Step 2: Register a voiceprint
If you've already started the voiceprint service, visit `http://localhost:8005/voiceprint/docs` in your local browser to see the API docs. Here we only describe how to use the register-voiceprint API.

The voiceprint registration API is at `http://localhost:8005/voiceprint/register`, method POST.

The request header needs a Bearer Token. The token is the part after `?key=` in your `Voiceprint API address`. For example, if my voiceprint registration address is `http://127.0.0.1:8005/voiceprint/health?key=abcd`, my token is `abcd`.

The request body contains the speaker ID (speaker_id) and a WAV audio file (file). Example request:

```
curl -X POST \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -F "speaker_id=your_speaker_id_here" \
  -F "file=@/path/to/your/file" \
  http://localhost:8005/voiceprint/register
```

 Here, `file` is the audio file of the speaker to be registered, and `speaker_id` must match the `speaker_id` configured in Step 1. For example, if I need to register Zhang San's voiceprint and Zhang San's `speaker_id` is `test1` in `.config.yaml`, then when registering, the request body's `speaker_id` must be `test1`, and `file` is the audio file of Zhang San speaking.

 ## Step 3: Start the services

Start the LittleWise server and the voiceprint service, and you're good to go.
