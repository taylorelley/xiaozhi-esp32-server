from config.logger import setup_logging
from http import HTTPStatus
import dashscope
from dashscope import Application
from core.providers.llm.base import LLMProviderBase
from core.utils.util import check_model_key
import time

TAG = __name__
logger = setup_logging()


class LLMProvider(LLMProviderBase):
    def __init__(self, config):
        self.api_key = config["api_key"]
        self.app_id = config["app_id"]
        self.base_url = config.get("base_url")
        self.is_No_prompt = config.get("is_no_prompt")
        self.memory_id = config.get("ali_memory_id")
        self.streaming_chunk_size = config.get("streaming_chunk_size", 3)  # Number of characters per streamed chunk
        check_model_key("AliBLLLM", self.api_key)

    def response(self, session_id, dialogue):
        # Process dialogue
        if self.is_No_prompt:
            dialogue.pop(0)
            logger.bind(tag=TAG).debug(
                f"[Aliyun Bailian API service] Processed dialogue: {dialogue}"
            )

        # Build call parameters
        call_params = {
            "api_key": self.api_key,
            "app_id": self.app_id,
            "session_id": session_id,
            "messages": dialogue,
            # Enable SDK-native streaming
            "stream": True,
        }
        if self.memory_id != False:
            # Bailian memory requires a prompt parameter
            prompt = dialogue[-1].get("content")
            call_params["memory_id"] = self.memory_id
            call_params["prompt"] = prompt
            logger.bind(tag=TAG).debug(
                f"[Aliyun Bailian API service] Processed prompt: {prompt}"
            )

        # Optionally set a custom API base URL (ignored when compatibility-mode URL is configured)
        if self.base_url and ("/api/" in self.base_url):
            dashscope.base_http_api_url = self.base_url

        responses = Application.call(**call_params)

        # Streaming (SDK returns an iterable when stream=True; otherwise a single response object)
        logger.bind(tag=TAG).debug(
            f"[Aliyun Bailian API service] Call parameters: {dict(call_params, api_key='***')}"
        )

        last_text = ""
        try:
            for resp in responses:
                if resp.status_code != HTTPStatus.OK:
                    logger.bind(tag=TAG).error(
                        f"code={resp.status_code}, message={resp.message}, refer to docs: https://help.aliyun.com/zh/model-studio/developer-reference/error-code"
                    )
                    continue
                current_text = getattr(getattr(resp, "output", None), "text", None)
                if current_text is None:
                    continue
                # SDK streams as incremental overwrite, compute delta output
                if len(current_text) >= len(last_text):
                    delta = current_text[len(last_text):]
                else:
                    # Avoid occasional regressions
                    delta = current_text
                if delta:
                    yield delta
                last_text = current_text
        except TypeError:
            # Non-streaming fallback (single response)
            if responses.status_code != HTTPStatus.OK:
                logger.bind(tag=TAG).error(
                    f"code={responses.status_code}, message={responses.message}, refer to docs: https://help.aliyun.com/zh/model-studio/developer-reference/error-code"
                )
                yield "[Aliyun Bailian API service response error]"
            else:
                full_text = getattr(getattr(responses, "output", None), "text", "")
                logger.bind(tag=TAG).info(
                    f"[Aliyun Bailian API service] Full response length: {len(full_text)}"
                )
                for i in range(0, len(full_text), self.streaming_chunk_size):
                    chunk = full_text[i:i + self.streaming_chunk_size]
                    if chunk:
                        yield chunk

    def response_with_functions(self, session_id, dialogue, functions=None):
        # Aliyun Bailian currently does not support native function calls. To stay compatible, fall back to plain text streaming.
        # The caller consumes (content, tool_calls) tuples, so we always return (token, None) here.
        logger.bind(tag=TAG).warning(
            "Aliyun Bailian has no native function call support; falling back to plain text streaming"
        )
        for token in self.response(session_id, dialogue):
            yield token, None
