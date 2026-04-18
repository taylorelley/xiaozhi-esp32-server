import anthropic

from config.logger import setup_logging
from core.providers.vllm.base import VLLMProviderBase
from core.utils.util import check_model_key

TAG = __name__
logger = setup_logging()


class VLLMProvider(VLLMProviderBase):
    def __init__(self, config):
        self.model_name = config.get("model_name", "claude-sonnet-4-5")
        self.api_key = config.get("api_key")
        self.base_url = config.get("base_url") or config.get("url") or None
        self.max_tokens = int(config.get("max_tokens") or 1024)
        temp = config.get("temperature")
        self.temperature = float(temp) if temp not in (None, "") else None

        key_msg = check_model_key("VLLM", self.api_key)
        if key_msg:
            logger.bind(tag=TAG).error(key_msg)

        kwargs = {"api_key": self.api_key}
        if self.base_url:
            kwargs["base_url"] = self.base_url
        self.client = anthropic.Anthropic(**kwargs)

    def response(self, question, base64_image):
        question = (question or "") + "(Please reply in English)"
        try:
            params = {
                "model": self.model_name,
                "max_tokens": self.max_tokens,
                "messages": [
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image",
                                "source": {
                                    "type": "base64",
                                    "media_type": "image/jpeg",
                                    "data": base64_image,
                                },
                            },
                            {"type": "text", "text": question},
                        ],
                    }
                ],
            }
            if self.temperature is not None:
                params["temperature"] = self.temperature

            resp = self.client.messages.create(**params)
            parts = [b.text for b in resp.content if getattr(b, "type", None) == "text"]
            return "".join(parts)
        except Exception as e:
            logger.bind(tag=TAG).error(f"Anthropic VLLM error: {e}")
            raise
