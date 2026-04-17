# Weather Plugin Usage Guide

## Overview

The `get_weather` weather plugin is one of the core features of the LittleWise ESP32 voice assistant. It lets you query weather for any location in China via voice. The plugin is based on the QWeather API and provides real-time weather and 7-day weather forecasts.

## How to Obtain an API Key

### 1. Register a QWeather account

1. Visit the [QWeather console](https://console.qweather.com/).
2. Register an account and complete email verification.
3. Log in to the console.

### 2. Create a project and get an API Key

1. After entering the console, click ["Project Management"](https://console.qweather.com/project?lang=zh) on the right side, then click "Create Project".
2. Fill in the project information:
   - **Project name**: e.g., "LittleWise Voice Assistant"
3. Click Save.
4. After the project is created, click "Create Credential" within that project.
5. Fill in the credential information:
    - **Credential name**: e.g., "LittleWise Voice Assistant"
    - **Authentication method**: choose "API Key"
6. Click Save.
7. Copy the `API Key` from the credential. This is the first critical piece of configuration.

### 3. Get the API Host

1. In the console, click ["Settings"](https://console.qweather.com/setting?lang=zh) -> "API Host".
2. View the dedicated `API Host` assigned to you. This is the second critical piece of configuration.

After the steps above, you will have two important pieces of configuration information: `API Key` and `API Host`.

## Configuration (choose one)

### Method 1: If you're using a control console deployment (recommended)

1. Log in to the control console.
2. Go to the "Role Configuration" page.
3. Select the agent to configure.
4. Click "Edit Features".
5. In the parameter configuration area on the right, find the "Weather Query" plugin.
6. Check "Weather Query".
7. Paste the first key config, `API Key`, into the `Weather plugin API key` field.
8. Paste the second key config, `API Host`, into the `Developer API Host` field.
9. Save the configuration, then save the agent configuration.

### Method 2: If you're doing a single-module xiaozhi-server deployment only

Configure in `data/.config.yaml`:

1. Paste the first key config, `API Key`, into `api_key`.
2. Paste the second key config, `API Host`, into `api_host`.
3. Fill in your city in `default_location`, e.g., `Guangzhou`.

```yaml
plugins:
  get_weather:
    api_key: "your QWeather API key"
    api_host: "your QWeather API host address"
    default_location: "your default query city"
```
