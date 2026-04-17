# ESP32 Firmware Build

## Step 1: Prepare your OTA address

If you are using version 0.3.12 of this project, regardless of whether it is a simple Server deployment or full-module deployment, you will have an OTA address.

Since the OTA address setup differs between simple Server and full-module deployments, please choose the appropriate option below:

### If you're using a simple Server deployment
Now, open your OTA address in a browser. For example, my OTA address is:
```
http://192.168.1.25:8003/xiaozhi/ota/
```
If it shows "OTA interface is running normally, the websocket address to be sent to the device is: ws://xxx:8000/xiaozhi/v1/"

You can use the `test_page.html` provided by the project to test whether you can connect to the websocket address shown on the OTA page.

If you can't access it, you need to modify the `server.websocket` address in the configuration file `.config.yaml`, restart, and test again until `test_page.html` can connect.

Once successful, proceed to Step 2.

### If you're using a full-module deployment
Now, open your OTA address in a browser. For example, my OTA address is:
```
http://192.168.1.25:8002/xiaozhi/ota/
```

If it shows "OTA interface is running normally, websocket cluster count: X", proceed to Step 2.

If it shows "OTA interface is not running normally", it's probably because you haven't configured the `Websocket` address in the `Control Console`. Then:

- 1. Log in to the control console as a super administrator.

- 2. Click `Parameter Management` in the top menu.

- 3. Find the item `server.websocket` in the list and enter your `Websocket` address. For example, mine is:

```
ws://192.168.1.25:8000/xiaozhi/v1/
```

After configuring, refresh your OTA interface address in the browser and see whether it's normal. If not, double-check whether Websocket started correctly and whether the Websocket address is configured.

## Step 2: Configure the environment
First follow this tutorial to set up the project environment: [Setting up ESP IDF 5.3.2 development environment on Windows and building LittleWise](https://icnynnzcwou8.feishu.cn/wiki/JEYDwTTALi5s2zkGlFGcDiRknXf).

## Step 3: Open the configuration file
After the build environment is configured, download Brother Xia's xiaozhi-esp32 project source code.

Download Brother Xia's [xiaozhi-esp32 project source code](https://github.com/78/xiaozhi-esp32) from here.

After downloading, open the file `xiaozhi-esp32/main/Kconfig.projbuild`.

## Step 4: Modify the OTA address

Find the `default` value of `OTA_URL` and change `https://api.tenclass.net/xiaozhi/ota/`
to your own address. For example, my API address is `http://192.168.1.25:8002/xiaozhi/ota/`, so change the value to that.

Before:
```
config OTA_URL
    string "Default OTA URL"
    default "https://api.tenclass.net/xiaozhi/ota/"
    help
        The application will access this URL to check for new firmwares and server address.
```
After:
```
config OTA_URL
    string "Default OTA URL"
    default "http://192.168.1.25:8002/xiaozhi/ota/"
    help
        The application will access this URL to check for new firmwares and server address.
```

## Step 4: Set build parameters

Set the build parameters:

```
# In the terminal, enter the xiaozhi-esp32 root directory
cd xiaozhi-esp32
# For example, my board is esp32s3, so I set the build target to esp32s3. If your board is a different model, replace it accordingly.
idf.py set-target esp32s3
# Enter the menu configuration
idf.py menuconfig
```

After entering menuconfig, go into `Xiaozhi Assistant` and set `BOARD_TYPE` to the specific model of your board.
Save and exit, returning to the terminal.

## Step 5: Build the firmware

```
idf.py build
```

## Step 6: Package the bin firmware

```
cd scripts
python release.py
```

After the packaging command above completes, a firmware file `merged-binary.bin` will be generated in the `build` directory in the project root.
This `merged-binary.bin` is the firmware file to flash to your hardware.

Note: if, after running the second command, you get a "zip"-related error, please ignore it. As long as the firmware file `merged-binary.bin` is generated in the `build` directory, it won't affect you. Continue.

## Step 7: Flash the firmware
   Connect the ESP32 device to your computer, use Chrome browser, and open:

```
https://espressif.github.io/esp-launchpad/
```

Open this tutorial: [Flash tool / Web-based firmware flashing (no IDF dev environment)](https://ccnphfhqs21z.feishu.cn/wiki/Zpz4wXBtdimBrLk25WdcXzxcnNS).
Scroll to: `Method 2: ESP-Launchpad browser-based flashing`, and start from `3. Flash/download firmware to development board`. Follow the tutorial.

After flashing succeeds and the device connects to Wi-Fi, wake up LittleWise with the wake word and watch the console output from the server side.

## FAQ
Here are some common questions for reference:

[1. Why does LittleWise recognize a lot of what I say as Korean, Japanese, or English?](./FAQ.md)

[2. Why do I get "TTS task error: file does not exist"?](./FAQ.md)

[3. TTS frequently fails or times out](./FAQ.md)

[4. I can connect to the self-hosted server over Wi-Fi, but 4G mode can't connect](./FAQ.md)

[5. How can I improve LittleWise's conversation response speed?](./FAQ.md)

[6. I speak slowly, and LittleWise keeps interrupting me during pauses](./FAQ.md)

[7. I want to use LittleWise to control lights, air conditioners, remote power on/off, etc.](./FAQ.md)
