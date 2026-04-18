import json
from types import SimpleNamespace

import anthropic

from config.logger import setup_logging
from core.providers.llm.base import LLMProviderBase
from core.utils.util import check_model_key

TAG = __name__
logger = setup_logging()


def _translate_dialogue(dialogue):
    """Convert an OpenAI-style dialogue list to Anthropic ``system`` + ``messages``.

    The server's handlers produce messages in OpenAI shape (system/user/assistant
    with optional ``tool_calls`` and a ``tool`` role for results). Anthropic
    expects a top-level ``system`` string plus a list of user/assistant messages
    whose content is a list of typed blocks.
    """
    system_parts = []
    messages = []
    for msg in dialogue:
        role = msg.get("role")
        content = msg.get("content", "")

        if role == "system":
            if content:
                system_parts.append(content)
            continue

        if role == "tool":
            messages.append(
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "tool_result",
                            "tool_use_id": msg.get("tool_call_id", ""),
                            "content": content or "",
                        }
                    ],
                }
            )
            continue

        if role == "assistant" and msg.get("tool_calls"):
            blocks = []
            if content:
                blocks.append({"type": "text", "text": content})
            for tc in msg["tool_calls"]:
                fn = tc.get("function") if isinstance(tc, dict) else None
                name = (fn or {}).get("name") or tc.get("name", "")
                raw_args = (fn or {}).get("arguments") or tc.get("arguments", "")
                try:
                    args = json.loads(raw_args) if isinstance(raw_args, str) else raw_args
                except (TypeError, ValueError):
                    args = {}
                blocks.append(
                    {
                        "type": "tool_use",
                        "id": tc.get("id", ""),
                        "name": name,
                        "input": args or {},
                    }
                )
            messages.append({"role": "assistant", "content": blocks})
            continue

        messages.append({"role": role or "user", "content": content or ""})

    return "\n\n".join(system_parts) if system_parts else None, messages


def _translate_tools(functions):
    """Translate OpenAI function/tool definitions to Anthropic's tool schema."""
    if not functions:
        return None
    tools = []
    for fn in functions:
        spec = fn.get("function", fn) if isinstance(fn, dict) else {}
        tools.append(
            {
                "name": spec.get("name"),
                "description": spec.get("description", ""),
                "input_schema": spec.get("parameters")
                or {"type": "object", "properties": {}},
            }
        )
    return tools


def _openai_shaped_tool_call(index, tool_id, name, arguments):
    """Return an object that mimics the OpenAI delta.tool_calls[i] shape."""
    return SimpleNamespace(
        index=index,
        id=tool_id,
        function=SimpleNamespace(name=name, arguments=arguments),
    )


class LLMProvider(LLMProviderBase):
    def __init__(self, config):
        self.model_name = config.get("model_name", "claude-sonnet-4-5")
        self.api_key = config.get("api_key")
        self.base_url = config.get("base_url") or None
        self.max_tokens = int(config.get("max_tokens") or 1024)
        temp = config.get("temperature")
        self.temperature = float(temp) if temp not in (None, "") else None

        key_msg = check_model_key("LLM", self.api_key)
        if key_msg:
            logger.bind(tag=TAG).error(key_msg)

        kwargs = {"api_key": self.api_key}
        if self.base_url:
            kwargs["base_url"] = self.base_url
        self.client = anthropic.Anthropic(**kwargs)

    def _request_params(self, dialogue, **kwargs):
        system, messages = _translate_dialogue(dialogue)
        params = {
            "model": self.model_name,
            "max_tokens": int(kwargs.get("max_tokens") or self.max_tokens),
            "messages": messages,
        }
        if system:
            params["system"] = system
        temperature = kwargs.get("temperature", self.temperature)
        if temperature is not None:
            params["temperature"] = float(temperature)
        return params

    def response(self, session_id, dialogue, **kwargs):
        params = self._request_params(dialogue, **kwargs)
        try:
            with self.client.messages.stream(**params) as stream:
                for text in stream.text_stream:
                    if text:
                        yield text
        except Exception as e:
            logger.bind(tag=TAG).error(f"Anthropic response error: {e}")

    def response_with_functions(self, session_id, dialogue, functions=None, **kwargs):
        params = self._request_params(dialogue, **kwargs)
        tools = _translate_tools(functions)
        if tools:
            params["tools"] = tools

        try:
            with self.client.messages.stream(**params) as stream:
                tool_state = {}  # block_index -> {"id", "name", "args"}
                for event in stream:
                    etype = getattr(event, "type", None)

                    if etype == "content_block_start":
                        block = getattr(event, "content_block", None)
                        if getattr(block, "type", None) == "tool_use":
                            tool_state[event.index] = {
                                "id": block.id,
                                "name": block.name,
                                "args": "",
                            }
                        continue

                    if etype == "content_block_delta":
                        delta = getattr(event, "delta", None)
                        dtype = getattr(delta, "type", None)
                        if dtype == "text_delta":
                            text = getattr(delta, "text", "")
                            if text:
                                yield text, None
                        elif dtype == "input_json_delta":
                            partial = getattr(delta, "partial_json", "") or ""
                            state = tool_state.get(event.index)
                            if state is None:
                                continue
                            first = state["args"] == ""
                            state["args"] += partial
                            yield None, [
                                _openai_shaped_tool_call(
                                    index=event.index,
                                    tool_id=state["id"] if first else "",
                                    name=state["name"] if first else "",
                                    arguments=partial,
                                )
                            ]
                        continue

                    if etype == "content_block_stop":
                        state = tool_state.get(event.index)
                        # If the tool block carried no incremental JSON deltas
                        # (Anthropic can emit the whole input in one block),
                        # surface it once on close so the caller still sees it.
                        if state is not None and state["args"] == "":
                            block = getattr(event, "content_block", None)
                            full = getattr(block, "input", None)
                            if full is not None:
                                args_str = json.dumps(full)
                                state["args"] = args_str
                                yield None, [
                                    _openai_shaped_tool_call(
                                        index=event.index,
                                        tool_id=state["id"],
                                        name=state["name"],
                                        arguments=args_str,
                                    )
                                ]
                        continue
        except Exception as e:
            logger.bind(tag=TAG).error(f"Anthropic response_with_functions error: {e}")
