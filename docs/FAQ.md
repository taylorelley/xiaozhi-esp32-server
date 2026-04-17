# Frequently Asked Questions

### 1. Why does LittleWise recognize a lot of what I say as Korean, Japanese, or English?

Suggestion: Check whether `models/SenseVoiceSmall` already contains the `model.pt`
file. If not, you need to download it. See [Download speech recognition model file](Deployment.md#model-files).

### 2. Why do I get "TTS task error: file does not exist"?

Suggestion: Check whether you have correctly installed the `libopus` and `ffmpeg` libraries via `conda`.

If not installed, install them:

```
conda install conda-forge::libopus
conda install conda-forge::ffmpeg
```

### 3. TTS frequently fails or times out

Suggestion: If `EdgeTTS` fails frequently, first check whether you are using a proxy (VPN). If so, try disabling the proxy and try again.
If you are using Volcano Engine's Doubao TTS, and it fails frequently, it is recommended to use the paid version, because the trial version only supports 2 concurrent connections.

### 4. I can connect to the self-hosted server over Wi-Fi, but 4G mode can't connect

Reason: Brother Xia's firmware requires a secure connection in 4G mode.

Solution: There are currently two ways to solve this. Pick either one:

1. Modify the code. Follow this video for a solution: https://www.bilibili.com/video/BV18MfTYoE85

2. Configure SSL certificates via Nginx. Reference tutorial: https://icnt94i5ctj4.feishu.cn/docx/GnYOdMNJOoRCljx1ctecsj9cnRe

### 5. How can I improve LittleWise's conversation response speed?

This project is configured by default with a low-cost setup. Beginners are encouraged to start with the default free models to first solve "getting it to run", and then optimize for "running fast".
To improve response speed, try swapping out the components. Since version `0.5.2`, the project supports a streaming configuration that improves response speed by about `2.5 seconds` compared to earlier versions, significantly improving user experience.

| Module | Entry-level all-free setup | Streaming configuration |
|:---:|:---:|:---:|
| ASR (Speech Recognition) | FunASR (local) | XunfeiStreamASR (iFlytek streaming, recommended) |
| LLM (Large Language Model) | glm-4-flash (Zhipu) | qwen-flash (Alibaba Bailian, recommended) |
| VLLM (Vision LLM) | glm-4v-flash (Zhipu) | qwen3.5-flash (Alibaba Bailian, recommended) |
| TTS (Text-to-Speech) | EdgeTTS (Microsoft) | HuoshanDoubleStreamTTS (Volcano streaming, recommended) |
| Intent (Intent Recognition) | function_call | function_call |
| Memory | mem_local_short (local short-term memory) | mem_local_short (local short-term memory) |

If you care about the latency of each component, please refer to the [LittleWise component performance test report](https://github.com/xinnan-tech/xiaozhi-performance-research). You can test in your own environment using the test methods described in the report.

### 6. I speak slowly, and LittleWise keeps interrupting me during pauses

Suggestion: In the configuration file, find the following section and increase the value of `min_silence_duration_ms` (e.g., change it to `1000`):

```yaml
VAD:
  SileroVAD:
    threshold: 0.5
    model_dir: models/snakers4_silero-vad
    min_silence_duration_ms: 700  # If you pause a lot while speaking, increase this value
```

### 7. Deployment tutorials
1. [How to do a minimal deployment](./Deployment.md)<br/>
2. [How to do a full-module deployment](./Deployment_all.md)<br/>
3. [How to deploy an MQTT gateway to enable the MQTT+UDP protocol](./mqtt-gateway-integration.md)<br/>
4. [How to automatically pull the latest code of this project, auto-compile and start](./dev-ops-integration.md)<br/>
5. [How to integrate with Nginx](https://github.com/xinnan-tech/xiaozhi-esp32-server/issues/791)<br/>

### 9. Firmware build tutorials
1. [How to build the LittleWise firmware yourself](./firmware-build.md)<br/>
2. [How to modify the OTA address in the firmware pre-built by Brother Xia](./firmware-setting.md)<br/>
3. [How to configure firmware OTA auto-upgrade for single-module deployment](./ota-upgrade-guide.md)<br/>

### 10. Extension tutorials
1. [How to enable phone-number registration on the control console](./ali-sms-integration.md)<br/>
2. [How to integrate HomeAssistant for smart-home control](./homeassistant-integration.md)<br/>
3. [How to enable the vision model for photo-based object recognition](./mcp-vision-integration.md)<br/>
4. [How to deploy an MCP endpoint](./mcp-endpoint-enable.md)<br/>
5. [How to connect to an MCP endpoint](./mcp-endpoint-integration.md)<br/>
6. [How the MCP method obtains device information](./mcp-get-device-info.md)<br/>
7. [How to enable voiceprint recognition](./voiceprint-integration.md)<br/>
8. [News plugin source configuration guide](./newsnow_plugin_config.md)<br/>
9. [Knowledge base ragflow integration guide](./ragflow-integration.md)<br/>
10. [How to deploy a context provider](./context-provider-integration.md)<br/>
11. [How to integrate PowerMem smart memory](./powermem-integration.md)<br/>
12. [How to configure the weather plugin to query weather](./weather-integration.md)<br/>

### 11. Voice cloning and local voice deployment tutorials
1. [How to clone a voice on the control console](./huoshan-streamTTS-voice-cloning.md)<br/>
2. [How to deploy and integrate index-tts local voice](./index-stream-integration.md)<br/>
3. [How to deploy and integrate fish-speech local voice](./fish-speech-integration.md)<br/>
4. [How to deploy and integrate PaddleSpeech local voice](./paddlespeech-deploy.md)<br/>

### 12. Performance testing tutorials
1. [Component speed test guide](./performance_tester.md)<br/>
2. [Periodically published test results](https://github.com/xinnan-tech/xiaozhi-performance-research)<br/>

### 13. For more questions, feel free to contact us

You can submit your questions at [issues](https://github.com/xinnan-tech/xiaozhi-esp32-server/issues).
