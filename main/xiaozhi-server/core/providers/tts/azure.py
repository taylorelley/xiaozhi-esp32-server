import html

import requests

from config.logger import setup_logging
from core.providers.tts.base import TTSProviderBase
from core.utils.util import check_model_key

TAG = __name__
logger = setup_logging()


_FORMAT_HEADERS = {
    "mp3": "audio-24khz-48kbitrate-mono-mp3",
    "wav": "riff-24khz-16bit-mono-pcm",
    "ogg": "ogg-24khz-16bit-mono-opus",
}


class TTSProvider(TTSProviderBase):
    """Azure Cognitive Services Speech text-to-speech provider.

    Uses the region-scoped REST endpoint with subscription-key auth so we
    don't have to pull in the heavy native ``azure-cognitiveservices-speech``
    SDK. Default voice is ``en-US-AriaNeural``.
    """

    def __init__(self, config, delete_audio_file):
        super().__init__(config, delete_audio_file)
        self.api_key = config.get("api_key")
        self.region = config.get("region", "eastus")
        self.voice = (
            config.get("private_voice")
            or config.get("voice")
            or "en-US-AriaNeural"
        )
        self.language = config.get("language") or self._language_from_voice(self.voice)
        self.audio_file_type = config.get("format", "mp3")
        self.output_format = _FORMAT_HEADERS.get(
            self.audio_file_type, _FORMAT_HEADERS["mp3"]
        )
        self.endpoint = config.get(
            "endpoint",
            f"https://{self.region}.tts.speech.microsoft.com/cognitiveservices/v1",
        )

        key_msg = check_model_key("TTS", self.api_key)
        if key_msg:
            logger.bind(tag=TAG).error(key_msg)

    @staticmethod
    def _language_from_voice(voice):
        # Azure voice names start with the locale, e.g. "en-US-AriaNeural".
        parts = voice.split("-", 2)
        if len(parts) >= 2:
            return f"{parts[0]}-{parts[1]}"
        return "en-US"

    def _build_ssml(self, text):
        safe_text = html.escape(text or "")
        return (
            f'<speak version="1.0" xml:lang="{self.language}">'
            f'<voice xml:lang="{self.language}" name="{self.voice}">'
            f"{safe_text}"
            f"</voice></speak>"
        )

    async def text_to_speak(self, text, output_file):
        headers = {
            "Ocp-Apim-Subscription-Key": self.api_key or "",
            "Content-Type": "application/ssml+xml",
            "X-Microsoft-OutputFormat": self.output_format,
            "User-Agent": "xiaozhi-esp32-server",
        }
        response = requests.post(
            self.endpoint,
            data=self._build_ssml(text).encode("utf-8"),
            headers=headers,
            timeout=60,
        )
        if response.status_code != 200:
            raise Exception(
                f"Azure TTS request failed: {response.status_code} - {response.text}"
            )

        if output_file:
            with open(output_file, "wb") as f:
                f.write(response.content)
            return None
        return response.content
