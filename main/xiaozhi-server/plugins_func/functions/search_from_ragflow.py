import requests
import sys
from config.logger import setup_logging
from plugins_func.register import register_function, ToolType, ActionResponse, Action
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler

TAG = __name__
logger = setup_logging()

# Base function description template
SEARCH_FROM_RAGFLOW_FUNCTION_DESC = {
    "type": "function",
    "function": {
        "name": "search_from_ragflow",
        "description": "Query information from the knowledge base",
        "parameters": {
            "type": "object",
            "properties": {"question": {"type": "string", "description": "The question to query"}},
            "required": ["question"],
        },
    },
}


@register_function(
    "search_from_ragflow", SEARCH_FROM_RAGFLOW_FUNCTION_DESC, ToolType.SYSTEM_CTL
)
def search_from_ragflow(conn: "ConnectionHandler", question=None):
    # Make sure string parameters are handled with the correct encoding
    if question and isinstance(question, str):
        # The question is already a UTF-8 encoded string
        pass
    else:
        question = str(question) if question is not None else ""

    ragflow_config = conn.config.get("plugins", {}).get("search_from_ragflow", {})
    base_url = ragflow_config.get("base_url", "")
    api_key = ragflow_config.get("api_key", "")
    dataset_ids = ragflow_config.get("dataset_ids", [])

    url = base_url + "/api/v1/retrieval"
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    # Ensure the strings in the payload are UTF-8 encoded
    payload = {"question": question, "dataset_ids": dataset_ids}

    try:
        # Use ensure_ascii=False so Chinese characters serialize correctly
        response = requests.post(
            url,
            json=payload,
            headers=headers,
            timeout=5,
            verify=False,
        )

        # Force the response encoding to utf-8
        response.encoding = "utf-8"

        response.raise_for_status()

        # Read the text first, then decode the JSON manually
        response_text = response.text
        import json

        result = json.loads(response_text)

        if result.get("code") != 0:
            error_detail = result.get("error", {}).get("detail", "Unknown error")
            error_message = result.get("error", {}).get("message", "")
            error_code = result.get("code", "")

            # Safely log the error information
            logger.bind(tag=TAG).error(
                f"RAGFlow API call failed. Response code: {error_code}; error detail: {error_detail}; full response: {result}"
            )

            # Build a detailed error response
            error_response = f"The RAG endpoint returned an error (error code: {error_code})"

            if error_message:
                error_response += f": {error_message}"
            if error_detail:
                error_response += f"\nDetail: {error_detail}"

            return ActionResponse(Action.RESPONSE, None, error_response)

        chunks = result.get("data", {}).get("chunks", [])
        contents = []
        for chunk in chunks:
            content = chunk.get("content", "")
            if content:
                # Safely process the content string
                if isinstance(content, str):
                    contents.append(content)
                elif isinstance(content, bytes):
                    contents.append(content.decode("utf-8", errors="replace"))
                else:
                    contents.append(str(content))

        if contents:
            # Format the knowledge base content as a reference block
            context_text = f"# Knowledge base results for the question [{question}]\n"
            context_text += "```\n\n\n".join(contents[:5])
            context_text += "\n```"
        else:
            context_text = "According to the knowledge base query, no relevant information was found."
        return ActionResponse(Action.REQLLM, context_text, None)

    except requests.exceptions.RequestException as e:
        # Network request exception
        error_type = type(e).__name__
        logger.bind(tag=TAG).error(
            f"RAGflow network request failed. Exception type: {error_type}; detail: {str(e)}"
        )

        # Provide more detailed error information and a resolution based on the exception type
        if isinstance(e, requests.exceptions.ConnectTimeout):
            error_response = "RAG endpoint connection timed out (5 seconds)"
            error_response += "\nPossible cause: the RAGflow service is not running or there are network issues"
            error_response += "\nSuggested fix: check the RAGflow service status and the network connection"

        elif isinstance(e, requests.exceptions.ConnectionError):
            error_response = "Unable to connect to the RAG endpoint"
            error_response += "\nPossible cause: the RAGflow service address is wrong or the service is not running"
            error_response += "\nSuggested fix: verify the RAGflow service address configuration and status"

        elif isinstance(e, requests.exceptions.Timeout):
            error_response = "RAG endpoint request timed out"
            error_response += "\nPossible cause: the RAGflow service is slow or the network is lagging"
            error_response += "\nSuggested fix: try again later, or check the performance of the RAGflow service"

        elif isinstance(e, requests.exceptions.HTTPError):
            # Handle HTTP error status codes
            if hasattr(e.response, "status_code"):
                status_code = e.response.status_code
                error_response = f"RAG endpoint HTTP error (status code: {status_code})"

                # Try to extract the error message from the response body
                try:
                    error_detail = e.response.json().get("error", {}).get("message", "")
                    if error_detail:
                        error_response += f"\nError detail: {error_detail}"
                except:
                    pass
            else:
                error_response = f"RAG endpoint HTTP exception: {str(e)}"

        else:
            error_response = f"RAG endpoint network exception ({error_type}): {str(e)}"

        return ActionResponse(Action.RESPONSE, None, error_response)

    except Exception as e:
        # Other exceptions
        error_type = type(e).__name__
        logger.bind(tag=TAG).error(
            f"RAGflow processing exception. Exception type: {error_type}; detail: {str(e)}"
        )

        # Provide detailed error information
        error_response = f"RAG endpoint processing exception ({error_type}): {str(e)}"
        return ActionResponse(Action.RESPONSE, None, error_response)
