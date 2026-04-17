# Configuring a Custom Server with Firmware Pre-Built by Brother Xia

## Step 1: Confirm the version
Flash the [1.6.1 or newer firmware](https://github.com/78/xiaozhi-esp32/releases) already built by Brother Xia.

## Step 2: Prepare your OTA address
If you followed the tutorial and did a full-module deployment, you should already have an OTA address.

Now, open your OTA address in a browser. For example, my OTA address is:
```
https://2662r3426b.vicp.fun/xiaozhi/ota/
```

If it shows "OTA interface is running normally, websocket cluster count: X", then continue.

If it shows "OTA interface is not running normally", it is probably because you haven't configured the `Websocket` address in the `Control Console`. In that case:

- 1. Log in to the control console as a super administrator.

- 2. Click `Parameter Management` in the top menu.

- 3. Find the item `server.websocket` in the list and enter your `Websocket` address. For example, mine is:

```
wss://2662r3426b.vicp.fun/xiaozhi/v1/
```

After configuring, refresh your OTA interface address in the browser and see whether it is now normal. If it's still not working, double-check whether your Websocket service is running properly and whether the Websocket address is configured correctly.

## Step 3: Enter Wi-Fi provisioning mode
Enter the device's Wi-Fi provisioning mode. At the top of the page, click "Advanced Options", enter your server's `ota` address, click Save, and restart the device.
![Please refer to - OTA address setup](../docs/images/firmware-setting-ota.png)

## Step 4: Wake up LittleWise and check the log output

Wake up LittleWise and see whether the logs are being output properly.


## FAQ
Here are some common questions for reference:

[1. Why does LittleWise recognize a lot of what I say as Korean, Japanese, or English?](./FAQ.md)

[2. Why do I get "TTS task error: file does not exist"?](./FAQ.md)

[3. TTS frequently fails or times out](./FAQ.md)

[4. I can connect to the self-hosted server over Wi-Fi, but 4G mode can't connect](./FAQ.md)

[5. How can I improve LittleWise's conversation response speed?](./FAQ.md)

[6. I speak slowly, and LittleWise keeps interrupting me during pauses](./FAQ.md)

[7. I want to use LittleWise to control lights, air conditioners, remote power on/off, etc.](./FAQ.md)
