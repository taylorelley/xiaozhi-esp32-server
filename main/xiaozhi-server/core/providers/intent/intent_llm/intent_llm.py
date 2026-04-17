from typing import List, Dict, TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler
from ..base import IntentProviderBase
from plugins_func.functions.play_music import initialize_music_handler
from config.logger import setup_logging
from core.utils.util import get_system_error_response
import re
import json
import hashlib
import time



TAG = __name__
logger = setup_logging()


class IntentProvider(IntentProviderBase):
    def __init__(self, config):
        super().__init__(config)
        self.llm = None
        self.promot = ""
        # Import the global cache manager
        from core.utils.cache.manager import cache_manager, CacheType

        self.cache_manager = cache_manager
        self.CacheType = CacheType
        self.history_count = 4  # By default, use the most recent 4 conversation records

    def get_intent_system_prompt(self, functions_list: str) -> str:
        """
        Dynamically generate the system prompt based on the configured intent options and available functions.
        Args:
            functions: List of available functions, as a JSON-formatted string
        Returns:
            The formatted system prompt
        """

        # Build the function description section
        functions_desc = "List of available functions:\n"
        for func in functions_list:
            func_info = func.get("function", {})
            name = func_info.get("name", "")
            desc = func_info.get("description", "")
            params = func_info.get("parameters", {})

            functions_desc += f"\nFunction name: {name}\n"
            functions_desc += f"Description: {desc}\n"

            if params:
                functions_desc += "Parameters:\n"
                for param_name, param_info in params.get("properties", {}).items():
                    param_desc = param_info.get("description", "")
                    param_type = param_info.get("type", "")
                    functions_desc += f"- {param_name} ({param_type}): {param_desc}\n"

            functions_desc += "---\n"

        prompt = (
            "[STRICT FORMAT REQUIREMENT] You must only return JSON format, and must absolutely not return any natural language!\n\n"
            "You are an intent recognition assistant. Please analyze the user's last sentence, determine the user's intent, and invoke the corresponding function.\n\n"
            "[IMPORTANT RULES] The following types of queries should directly return result_for_context, with no need to call a function:\n"
            "- Asking about the current time (e.g.: what time is it now, current time, query the time, etc.)\n"
            "- Asking about today's date (e.g.: what's the date today, what day of the week is it, what date is today, etc.)\n"
            "- Asking about today's lunar calendar (e.g.: what's today's lunar date, what solar term is today, etc.)\n"
            "- Asking about the current city (e.g.: where am I now, do you know which city I'm in, etc.)"
            "The system will directly construct an answer based on context information.\n\n"
            "- If the user uses interrogative words (such as 'how', 'why') to ask questions related to exiting (for example 'how did it exit?'), note that this is not telling you to exit; please return {'function_call': {'name': 'continue_chat'}\n"
            "- Only when the user explicitly uses commands like 'exit the system', 'end the conversation', 'I don't want to talk with you anymore', should handle_exit_intent be triggered\n\n"
            f"{functions_desc}\n"
            "Processing steps:\n"
            "1. Analyze the user's input and determine the user's intent\n"
            "2. Check whether it is one of the basic information queries described above (time, date, etc.); if so, return result_for_context\n"
            "3. Select the best-matching function from the available function list\n"
            "4. If a matching function is found, generate the corresponding function_call format\n"
            '5. If no matching function is found, return {"function_call": {"name": "continue_chat"}}\n\n'
            "Return format requirements:\n"
            "1. Must return pure JSON format, do not include any other text\n"
            "2. Must include the function_call field\n"
            "3. function_call must include the name field\n"
            "4. If the function requires parameters, the arguments field must be included\n\n"
            "Examples:\n"
            "```\n"
            "User: What time is it now?\n"
            'Return: {"function_call": {"name": "result_for_context"}}\n'
            "```\n"
            "```\n"
            "User: What is the current battery level?\n"
            'Return: {"function_call": {"name": "get_battery_level", "arguments": {"response_success": "The current battery level is {value}%", "response_failure": "Unable to get the current battery percentage"}}}\n'
            "```\n"
            "```\n"
            "User: What is the current screen brightness?\n"
            'Return: {"function_call": {"name": "self_screen_get_brightness"}}\n'
            "```\n"
            "```\n"
            "User: Set the screen brightness to 50%\n"
            'Return: {"function_call": {"name": "self_screen_set_brightness", "arguments": {"brightness": 50}}}\n'
            "```\n"
            "```\n"
            "User: I want to end the conversation\n"
            'Return: {"function_call": {"name": "handle_exit_intent", "arguments": {"say_goodbye": "goodbye"}}}\n'
            "```\n"
            "```\n"
            "User: Hello\n"
            'Return: {"function_call": {"name": "continue_chat"}}\n'
            "```\n\n"
            "Notes:\n"
            "1. Only return JSON format, do not include any other text\n"
            '2. First check whether the user query is basic information (time, date, etc.); if so, return {"function_call": {"name": "result_for_context"}}, no arguments parameter is needed\n'
            '3. If no matching function is found, return {"function_call": {"name": "continue_chat"}}\n'
            "4. Ensure the returned JSON format is correct and contains all necessary fields\n"
            "5. result_for_context does not need any parameters; the system will automatically get information from the context\n"
            "Special notes:\n"
            "- When the user's single input contains multiple instructions (such as 'turn on the light and turn up the volume')\n"
            "- Please return a JSON array composed of multiple function_call entries\n"
            "- Example: {'function_calls': [{name:'light_on'}, {name:'volume_up'}]}\n\n"
            "[FINAL WARNING] It is absolutely forbidden to output any natural language, emoji, or explanatory text! Only valid JSON format may be output! Violation of this rule will cause a system error!"
        )
        return prompt

    def replyResult(self, text: str, original_text: str):
        try:
            llm_result = self.llm.response_no_stream(
                system_prompt=text,
                user_prompt="Please reply to the user in a human-like conversational tone based on the above content, keep it concise, and directly return the result. The user is now saying: "
                + original_text,
            )
            return llm_result
        except Exception as e:
            logger.bind(tag=TAG).error(f"Error in generating reply result: {e}")
            return get_system_error_response(self.config)

    async def detect_intent(
        self, conn: "ConnectionHandler", dialogue_history: List[Dict], text: str
    ) -> str:
        if not self.llm:
            raise ValueError("LLM provider not set")
        if conn.func_handler is None:
            return '{"function_call": {"name": "continue_chat"}}'

        # Record the overall start time
        total_start_time = time.time()

        # Print the model information being used
        model_info = getattr(self.llm, "model_name", str(self.llm.__class__.__name__))
        logger.bind(tag=TAG).debug(f"Using intent recognition model: {model_info}")

        # Compute the cache key
        cache_key = hashlib.md5((conn.device_id + text).encode()).hexdigest()

        # Check the cache
        cached_intent = self.cache_manager.get(self.CacheType.INTENT, cache_key)
        if cached_intent is not None:
            cache_time = time.time() - total_start_time
            logger.bind(tag=TAG).debug(
                f"Using cached intent: {cache_key} -> {cached_intent}, time taken: {cache_time:.4f}s"
            )
            return cached_intent

        if self.promot == "":
            functions = conn.func_handler.get_functions()
            if hasattr(conn, "mcp_client"):
                mcp_tools = conn.mcp_client.get_available_tools()
                if mcp_tools is not None and len(mcp_tools) > 0:
                    if functions is None:
                        functions = []
                    functions.extend(mcp_tools)

            self.promot = self.get_intent_system_prompt(functions)

        music_config = initialize_music_handler(conn)
        music_file_names = music_config["music_file_names"]
        prompt_music = f"{self.promot}\n<musicNames>{music_file_names}\n</musicNames>"

        home_assistant_cfg = conn.config["plugins"].get("home_assistant")
        if home_assistant_cfg:
            devices = home_assistant_cfg.get("devices", [])
        else:
            devices = []
        if len(devices) > 0:
            hass_prompt = "\nBelow is the list of my home's smart devices (location, device name, entity_id), which can be controlled via homeassistant\n"
            for device in devices:
                hass_prompt += device + "\n"
            prompt_music += hass_prompt

        logger.bind(tag=TAG).debug(f"User prompt: {prompt_music}")

        # Build the prompt from the user's conversation history
        msgStr = ""

        # Get the most recent conversation history
        start_idx = max(0, len(dialogue_history) - self.history_count)
        for i in range(start_idx, len(dialogue_history)):
            msgStr += f"{dialogue_history[i].role}: {dialogue_history[i].content}\n"

        msgStr += f"User: {text}\n"
        user_prompt = f"current dialogue:\n{msgStr}"

        # Record preprocessing completion time
        preprocess_time = time.time() - total_start_time
        logger.bind(tag=TAG).debug(f"Intent recognition preprocessing time: {preprocess_time:.4f}s")

        # Use the LLM for intent recognition
        llm_start_time = time.time()
        logger.bind(tag=TAG).debug(f"Starting LLM intent recognition call, model: {model_info}")

        try:
            intent = self.llm.response_no_stream(
                system_prompt=prompt_music, user_prompt=user_prompt
            )
        except Exception as e:
            logger.bind(tag=TAG).error(f"Error in intent detection LLM call: {e}")
            return '{"function_call": {"name": "continue_chat"}}'

        # Record LLM call completion time
        llm_time = time.time() - llm_start_time
        logger.bind(tag=TAG).debug(
            f"External LLM intent recognition completed, model: {model_info}, call time: {llm_time:.4f}s"
        )

        # Record post-processing start time
        postprocess_start_time = time.time()

        # Clean and parse the response
        intent = intent.strip()
        # Try to extract the JSON portion
        match = re.search(r"\{.*\}", intent, re.DOTALL)
        if match:
            intent = match.group(0)

        # Record total processing time
        total_time = time.time() - total_start_time
        logger.bind(tag=TAG).debug(
            f"[Intent recognition performance] model: {model_info}, total time: {total_time:.4f}s, LLM call: {llm_time:.4f}s, query: '{text[:20]}...'"
        )

        # Try to parse as JSON
        try:
            intent_data = json.loads(intent)
            # If it contains function_call, format it for processing
            if "function_call" in intent_data:
                function_data = intent_data["function_call"]
                function_name = function_data.get("name")
                function_args = function_data.get("arguments", {})

                # Log the recognized function call
                logger.bind(tag=TAG).info(
                    f"llm recognized intent: {function_name}, arguments: {function_args}"
                )

                # Handle different types of intents
                if function_name == "result_for_context":
                    # Handle basic information queries, build the result directly from context
                    logger.bind(tag=TAG).info(
                        "Detected result_for_context intent; will answer directly using context information"
                    )

                elif function_name == "continue_chat":
                    # Handle ordinary conversation
                    # Retain messages that are not tool-related
                    clean_history = [
                        msg
                        for msg in conn.dialogue.dialogue
                        if msg.role not in ["tool", "function"]
                    ]
                    conn.dialogue.dialogue = clean_history

                else:
                    # Handle function call
                    logger.bind(tag=TAG).info(f"Detected function call intent: {function_name}")

            # Unified cache handling and return
            self.cache_manager.set(self.CacheType.INTENT, cache_key, intent)
            postprocess_time = time.time() - postprocess_start_time
            logger.bind(tag=TAG).debug(f"Intent post-processing time: {postprocess_time:.4f}s")
            return intent
        except json.JSONDecodeError:
            # Post-processing time
            postprocess_time = time.time() - postprocess_start_time
            logger.bind(tag=TAG).error(
                f"Unable to parse intent JSON: {intent}, post-processing time: {postprocess_time:.4f}s"
            )
            # If parsing fails, default to returning the continue chat intent
            return '{"function_call": {"name": "continue_chat"}}'
