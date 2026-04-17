# Single-Module Deployment Firmware OTA Auto-Upgrade Configuration Guide

This tutorial walks you through configuring firmware OTA auto-upgrade in a **single-module deployment** scenario so that devices can automatically update their firmware.

If you're already using a **full-module deployment**, please ignore this tutorial.

## Feature overview

In single-module deployment, xiaozhi-server has built-in OTA firmware management that can automatically detect a device's version and push firmware upgrades. The system automatically matches and pushes the latest firmware version based on the device model and current version.

## Prerequisites

- You have successfully completed a **single-module deployment** and are running xiaozhi-server.
- The device can connect to the server normally.

## Step 1: Prepare the firmware file

### 1. Create the firmware directory

Firmware files must go under `data/bin/`. If this directory does not exist, create it manually:

```bash
mkdir -p data/bin
```

### 2. Firmware file naming rules

Firmware files must follow this naming format:

```
{device_model}_{version}.bin
```

**Naming rules:**
- `device_model`: the device model name, e.g., `lichuang-dev`, `bread-compact-wifi`, etc.
- `version`: the firmware version, which must start with a digit, and may contain digits, letters, dots, underscores, and hyphens, e.g., `1.6.6`, `2.0.0`, etc.
- The file extension must be `.bin`.

**Naming examples:**
```
bread-compact-wifi_1.6.6.bin
lichuang-dev_2.0.0.bin
```

### 3. Place the firmware file

Copy the prepared firmware file (the .bin file) to the `data/bin/` directory:

Important! To repeat three times: the upgrade bin file is `xiaozhi.bin`, not the full firmware file `merged-binary.bin`!

Important! To repeat three times: the upgrade bin file is `xiaozhi.bin`, not the full firmware file `merged-binary.bin`!

Important! To repeat three times: the upgrade bin file is `xiaozhi.bin`, not the full firmware file `merged-binary.bin`!

```bash
cp xiaozhi.bin data/bin/device_model_version.bin
```

For example:
```bash
cp xiaozhi.bin data/bin/bread-compact-wifi_1.6.6.bin
```

## Step 2: Configure the public access address (only for public deployments)

**Note: this step only applies to single-module public-network deployments.**

If your xiaozhi-server is deployed over the public internet (using a public IP or domain), you **must** configure the `server.vision_explain` parameter, because the OTA firmware download URL uses the domain and port from that configuration.

If you're doing a LAN deployment, you can skip this step.

### Why configure this parameter?

In single-module deployment, when the system generates the firmware download URL, it uses the domain and port from `vision_explain` as the base address. If this is not configured or is misconfigured, devices will not be able to reach the firmware download URL.

### How to configure

Open the `data/.config.yaml` file, find the `server` configuration section, and set the `vision_explain` parameter:

```yaml
server:
  vision_explain: http://your_domain_or_ip:port/mcp/vision/explain
```

**Configuration examples:**

LAN deployment (default):
```yaml
server:
  vision_explain: http://192.168.1.100:8003/mcp/vision/explain
```

Public-domain deployment:
```yaml
server:
  vision_explain: http://yourdomain.com:8003/mcp/vision/explain
```

### Notes

- The domain or IP must be reachable from the device.
- If you're deploying with Docker, you cannot use Docker-internal addresses such as 127.0.0.1 or localhost.
- If you're using an nginx reverse proxy, fill in the public-facing address and port, not the port the project itself runs on.


## Frequently Asked Questions

### 1. The device is not receiving firmware updates

**Possible causes and solutions:**

- Check whether the firmware file name matches the rule: `{model}_{version}.bin`.
- Check that the firmware file is placed correctly in `data/bin/`.
- Check that the device model matches the model in the firmware file name.
- Check that the firmware version is higher than the device's current version.
- Check the server logs to confirm that the OTA request is being handled properly.

### 2. The device reports that the download URL is unreachable

**Possible causes and solutions:**

- Check that the domain or IP in `server.vision_explain` is correct.
- Confirm the port is correctly configured (default 8003).
- If it's a public deployment, ensure that the device can access the public address.
- If it's a Docker deployment, ensure you're not using an internal address (127.0.0.1).
- Check that the firewall has opened the corresponding port.
- If you're using an nginx reverse proxy, fill in the public-facing address and port, not the port the project itself runs on.

### 3. How to check the device's current version

Check the OTA request log; the log will show the version reported by the device:

```
[ota_handler] - Device AA:BB:CC:DD:EE:FF firmware is already up to date: 1.6.6
```

### 4. Firmware file was placed but nothing happens

The system has a 30-second cache (by default). You can:
- Wait 30 seconds before letting the device issue another OTA request.
- Restart the xiaozhi-server service.
- Adjust `firmware_cache_ttl` to a shorter value.
