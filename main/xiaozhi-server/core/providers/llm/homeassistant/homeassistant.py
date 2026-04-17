import requests
from requests.exceptions import RequestException
from config.logger import setup_logging
from core.providers.llm.base import LLMProviderBase

TAG = __name__
logger = setup_logging()


class LLMProvider(LLMProviderBase):
    def __init__(self, config):
        self.agent_id = config.get("agent_id")  # Corresponds to agent_id
        self.api_key = config.get("api_key")
        self.base_url = config.get("base_url", config.get("url"))  # Use base_url by default
        self.api_url = f"{self.base_url}/api/conversation/process"  # Build the full API URL

    def response(self, session_id, dialogue, **kwargs):
        # Home Assistant's voice assistant has built-in intent handling; no need to use LittleWise's own.
        # Just forward the user's utterance to Home Assistant.

        # Extract the content of the last message whose role is 'user'
        input_text = None
        if isinstance(dialogue, list):  # Ensure dialogue is a list
            # Iterate in reverse to find the last 'user' message
            for message in reversed(dialogue):
                if message.get("role") == "user":  # Found a 'user' role message
                    input_text = message.get("content", "")
                    break  # Exit the loop once found

        # Build the request payload
        payload = {
            "text": input_text,
            "agent_id": self.agent_id,
            "conversation_id": session_id,  # Use session_id as conversation_id
        }
        # Set request headers
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        # Send POST request
        response = requests.post(self.api_url, json=payload, headers=headers)

        # Check whether the request succeeded
        response.raise_for_status()

        # Parse the response data
        data = response.json()
        speech = (
            data.get("response", {})
            .get("speech", {})
            .get("plain", {})
            .get("speech", "")
        )

        # Return the generated content
        if speech:
            yield speech
        else:
            logger.bind(tag=TAG).warning("API response contains no speech content")

    def response_with_functions(self, session_id, dialogue, functions=None):
        logger.bind(tag=TAG).error(
            f"homeassistant does not support function call; consider using another intent recognizer"
        )
