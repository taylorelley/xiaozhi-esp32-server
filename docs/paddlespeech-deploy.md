# PaddleSpeechTTS Integration with xiaozhi service

## Key notes
- Pros: local offline deployment, fast.
- Cons: As of September 25, 2025, the default model is a Chinese-only model and does not support English-to-speech. If your text contains English, it will not produce sound. To support both Chinese and English, you need to train the model yourself.

## 1. Base environment requirements
Operating system: Windows / Linux / WSL 2

Python version: 3.9 or later (please adjust according to the official Paddle tutorial).

Paddle version: the latest official version   ```https://www.paddlepaddle.org.cn/install```

Dependency management tool: conda or venv

## 2. Start the paddlespeech service
### 1. Clone the source code from the official paddlespeech repository
```bash 
git clone https://github.com/PaddlePaddle/PaddleSpeech.git
```
### 2. Create a virtual environment
```bash

conda create -n paddle_env python=3.10 -y
conda activate paddle_env
```
### 3. Install paddle
Due to differences in CPU/GPU architecture, please create the environment according to the Python version supported by the Paddle official guide.
```
https://www.paddlepaddle.org.cn/install
```

### 4. Enter the paddlespeech directory
```bash
cd PaddleSpeech
```
### 5. Install paddlespeech
```bash
pip install pytest-runner -i https://pypi.tuna.tsinghua.edu.cn/simple

# Use one of the following commands
pip install paddlepaddle -i https://mirror.baidu.com/pypi/simple
pip install paddlespeech -i https://pypi.tuna.tsinghua.edu.cn/simple
```
### 6. Automatically download the voice model via command
```bash
paddlespeech tts --input "你好，这是一次测试"
```
This step automatically downloads the model cache to the local `.paddlespeech/models` directory.

### 7. Modify the tts_online_application.yaml configuration
Refer to the path ```"PaddleSpeech\demos\streaming_tts_server\conf\tts_online_application.yaml"```.
Open the file ```tts_online_application.yaml``` with an editor and set ```protocol``` to ```websocket```.

### 8. Start the service
```yaml
paddlespeech_server start --config_file ./demos/streaming_tts_server/conf/tts_online_application.yaml
# Official default startup command:
paddlespeech_server start --config_file ./conf/tts_online_application.yaml
```
Start the command based on the actual path of your ```tts_online_application.yaml```. You'll know it started successfully when you see logs like:
```
Prefix dict has been built successfully.
[2025-08-07 10:03:11,312] [   DEBUG] __init__.py:166 - Prefix dict has been built successfully.
INFO:     Started server process [2298]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8092 (Press CTRL+C to quit)
```

## 3. Modify the LittleWise configuration file
### 1. ```main/xiaozhi-server/core/providers/tts/paddle_speech.py```

### 2. ```main/xiaozhi-server/data/.config.yaml```
Using single-module deployment:
```yaml
selected_module:
  TTS: PaddleSpeechTTS
TTS:
  PaddleSpeechTTS:
      type: paddle_speech
      protocol: websocket 
      url:  ws://127.0.0.1:8092/paddlespeech/tts/streaming  # URL of the TTS service, pointing to local server [websocket default ws://127.0.0.1:8092/paddlespeech/tts/streaming]
      spk_id: 0  # Speaker ID; 0 usually indicates the default speaker
      sample_rate: 24000  # Sample rate [websocket default 24000, http default 0 = auto]
      speed: 1.0  # Speech rate; 1.0 = normal, >1 = faster, <1 = slower
      volume: 1.0  # Volume; 1.0 = normal, >1 = louder, <1 = quieter
      save_path:   # Save path
```
### 3. Start the LittleWise service
```py
python app.py
```
Open `test/test_page.html` and test whether the paddlespeech side outputs logs when connecting and sending messages.

Example log output:
```
INFO:     127.0.0.1:44312 - "WebSocket /paddlespeech/tts/streaming" [accepted]
INFO:     connection open
[2025-08-07 11:16:33,355] [    INFO] - sentence: 哈哈，怎么突然找我聊天啦？
[2025-08-07 11:16:33,356] [    INFO] - The durations of audio is: 2.4625 s
[2025-08-07 11:16:33,356] [    INFO] - first response time: 0.1143045425415039 s
[2025-08-07 11:16:33,356] [    INFO] - final response time: 0.4777836799621582 s
[2025-08-07 11:16:33,356] [    INFO] - RTF: 0.19402382942625715
[2025-08-07 11:16:33,356] [    INFO] - Other info: front time: 0.06514096260070801 s, first am infer time: 0.008037090301513672 s, first voc infer time: 0.04112648963928223 s,
[2025-08-07 11:16:33,356] [    INFO] - Complete the synthesis of the audio streams
INFO:     connection closed

```
