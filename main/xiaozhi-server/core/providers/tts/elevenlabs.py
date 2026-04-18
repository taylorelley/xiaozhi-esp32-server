import requests

from config.logger import setup_logging
from core.providers.tts.base import TTSProviderBase
from core.utils.util import check_model_key

TAG = __name__
logger = setup_logging()


class TTSProvider(TTSProviderBase):
    """ElevenLabs text-to-speech provider.

    Streams MP3 audio back from ElevenLabs' REST endpoint. Default voice is the
    English "Rachel" preset so English users get a sensible out-of-the-box
    voice without picking a voice_id.
    """

    def __init__(self, config, delete_audio_file):
        super().__init__(config, delete_audio_file)
        self.api_key = config.get("api_key")
        # Default to "Rachel" (a widely-used English voice).
        self.voice_id = (
            config.get("private_voice")
            or config.get("voice_id")
            or "21m00Tcm4TlvDq8ikWAM"
        )
        self.model_id = config.get("model_id", "eleven_turbo_v2_5")
        self.api_url = config.get(
            "api_url", "https://api.elevenlabs.io/v1/text-to-speech"
        )
        # ElevenLabs returns MP3 by default.
        self.audio_file_type = config.get("format", "mp3")

        stability = config.get("stability")
        similarity = config.get("similarity_boost")
        self.stability = float(stability) if stability not in (None, "") else 0.5
        self.similarity_boost = (
            float(similarity) if similarity not in (None, "") else 0.75
        )

        key_msg = check_model_key("TTS", self.api_key)
        if key_msg:
            logger.bind(tag=TAG).error(key_msg)

    async def text_to_speak(self, text, output_file):
        url = f"{self.api_url.rstrip('/')}/{self.voice_id}"
        headers = {
            "xi-api-key": self.api_key or "",
            "accept": f"audio/{self.audio_file_type}",
            "content-type": "application/json",
        }
        payload = {
            "text": text,
            "model_id": self.model_id,
            "voice_settings": {
                "stability": self.stability,
                "similarity_boost": self.similarity_boost,
            },
        }

        response = requests.post(url, json=payload, headers=headers, timeout=60)
        if response.status_code != 200:
            raise Exception(
                f"ElevenLabs TTS request failed: {response.status_code} - {response.text}"
            )

        if output_file:
            with open(output_file, "wb") as f:
                f.write(response.content)
            return None
        return response.content
