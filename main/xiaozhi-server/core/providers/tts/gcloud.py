import base64

import requests

from config.logger import setup_logging
from core.providers.tts.base import TTSProviderBase
from core.utils.util import check_model_key

TAG = __name__
logger = setup_logging()


_ENCODINGS = {"mp3": "MP3", "wav": "LINEAR16", "ogg": "OGG_OPUS"}


class TTSProvider(TTSProviderBase):
    """Google Cloud Text-to-Speech provider (REST API, API-key auth).

    Uses the public v1 REST endpoint so we don't need the heavy
    ``google-cloud-texttospeech`` SDK or service-account JSON files.
    """

    def __init__(self, config, delete_audio_file):
        super().__init__(config, delete_audio_file)
        self.api_key = config.get("api_key")
        self.voice = (
            config.get("private_voice")
            or config.get("voice")
            or "en-US-Neural2-F"
        )
        self.language_code = (
            config.get("language_code") or self._language_from_voice(self.voice)
        )
        self.audio_file_type = config.get("format", "mp3")
        self.audio_encoding = _ENCODINGS.get(self.audio_file_type, "MP3")
        self.endpoint = config.get(
            "endpoint", "https://texttospeech.googleapis.com/v1/text:synthesize"
        )

        speed = config.get("speaking_rate", config.get("speed"))
        self.speaking_rate = float(speed) if speed not in (None, "") else 1.0
        pitch = config.get("pitch")
        self.pitch = float(pitch) if pitch not in (None, "") else 0.0

        key_msg = check_model_key("TTS", self.api_key)
        if key_msg:
            logger.bind(tag=TAG).error(key_msg)

    @staticmethod
    def _language_from_voice(voice):
        parts = voice.split("-", 2)
        if len(parts) >= 2:
            return f"{parts[0]}-{parts[1]}"
        return "en-US"

    async def text_to_speak(self, text, output_file):
        payload = {
            "input": {"text": text or ""},
            "voice": {"languageCode": self.language_code, "name": self.voice},
            "audioConfig": {
                "audioEncoding": self.audio_encoding,
                "speakingRate": self.speaking_rate,
                "pitch": self.pitch,
            },
        }
        params = {"key": self.api_key} if self.api_key else None
        response = requests.post(
            self.endpoint, json=payload, params=params, timeout=60
        )
        if response.status_code != 200:
            raise Exception(
                f"Google Cloud TTS request failed: {response.status_code} - {response.text}"
            )

        audio_b64 = response.json().get("audioContent")
        if not audio_b64:
            raise Exception("Google Cloud TTS response missing audioContent")
        audio_bytes = base64.b64decode(audio_b64)

        if output_file:
            with open(output_file, "wb") as f:
                f.write(audio_bytes)
            return None
        return audio_bytes
