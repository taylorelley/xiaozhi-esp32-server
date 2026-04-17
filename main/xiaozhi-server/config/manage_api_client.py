import os
import base64
from typing import Optional, Dict

import httpx

TAG = __name__


class DeviceNotFoundException(Exception):
    pass


class DeviceBindException(Exception):
    def __init__(self, bind_code):
        self.bind_code = bind_code
        super().__init__(f"Device binding exception; bind code: {bind_code}")


class ManageApiClient:
    _instance = None
    _async_clients = {}  # Keeps an independent client per event loop
    _secret = None

    def __new__(cls, config):
        """Singleton pattern ensuring a single global instance while still accepting configuration."""
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._init_client(config)
        return cls._instance

    @classmethod
    def _init_client(cls, config):
        """Initialize the configuration (lazy client creation)."""
        cls.config = config.get("manager-api")

        if not cls.config:
            raise Exception("manager-api configuration is invalid")

        if not cls.config.get("url") or not cls.config.get("secret"):
            raise Exception("manager-api url or secret is not configured")

        if "your" in cls.config.get("secret").lower():
            raise Exception("Please configure the manager-api secret first")

        cls._secret = cls.config.get("secret")
        cls.max_retries = cls.config.get("max_retries", 6)  # Maximum retry count
        cls.retry_delay = cls.config.get("retry_delay", 10)  # Initial retry delay (seconds)
        # Do not create the AsyncClient here; defer until it is actually needed
        cls._async_clients = {}

    @classmethod
    async def _ensure_async_client(cls):
        """Ensure the async client is created (creating a dedicated client per event loop)."""
        import asyncio

        try:
            loop = asyncio.get_running_loop()
            loop_id = id(loop)

            # Create a dedicated client for each event loop
            if loop_id not in cls._async_clients:
                # The server may close the connection proactively; the httpx connection pool
                # cannot always detect and clean those up correctly.
                limits = httpx.Limits(
                    max_keepalive_connections=0,  # Disable keep-alive so every request uses a new connection
                )
                cls._async_clients[loop_id] = httpx.AsyncClient(
                    base_url=cls.config.get("url"),
                    headers={
                        "User-Agent": f"PythonClient/2.0 (PID:{os.getpid()})",
                        "Accept": "application/json",
                        "Authorization": "Bearer " + cls._secret,
                    },
                    timeout=cls.config.get("timeout", 30),
                    limits=limits,  # Apply limits
                )
            return cls._async_clients[loop_id]
        except RuntimeError:
            # If there is no running event loop, create a temporary one
            raise Exception("Must be called from an async context")

    @classmethod
    async def _async_request(cls, method: str, endpoint: str, **kwargs) -> Dict:
        """Send a single async HTTP request and process the response."""
        # Ensure the client has been created
        client = await cls._ensure_async_client()
        endpoint = endpoint.lstrip("/")
        response = None
        try:
            response = await client.request(method, endpoint, **kwargs)
            response.raise_for_status()

            result = response.json()

            # Handle business errors returned by the API
            if result.get("code") == 10041:
                raise DeviceNotFoundException(result.get("msg"))
            elif result.get("code") == 10042:
                raise DeviceBindException(result.get("msg"))
            elif result.get("code") != 0:
                raise Exception(f"API returned an error: {result.get('msg', 'Unknown error')}")

            # Return the success data
            return result.get("data") if result.get("code") == 0 else None
        finally:
            # Ensure the response is closed (runs even on exception)
            if response is not None:
                await response.aclose()

    @classmethod
    def _should_retry(cls, exception: Exception) -> bool:
        """Decide whether the exception warrants a retry."""
        # Network-level errors
        if isinstance(
            exception, (httpx.ConnectError, httpx.TimeoutException, httpx.NetworkError)
        ):
            return True

        # HTTP status-code errors
        if isinstance(exception, httpx.HTTPStatusError):
            status_code = exception.response.status_code
            return status_code in [408, 429, 500, 502, 503, 504]

        return False

    @classmethod
    async def _execute_async_request(cls, method: str, endpoint: str, **kwargs) -> Dict:
        """Async request executor with retry logic."""
        import asyncio

        retry_count = 0

        while retry_count <= cls.max_retries:
            try:
                # Execute the async request
                return await cls._async_request(method, endpoint, **kwargs)
            except Exception as e:
                # Decide whether to retry
                if retry_count < cls.max_retries and cls._should_retry(e):
                    retry_count += 1
                    print(
                        f"{method} {endpoint} async request failed; retrying attempt {retry_count} in {cls.retry_delay:.1f} seconds"
                    )
                    await asyncio.sleep(cls.retry_delay)
                    continue
                else:
                    # No retry; re-raise
                    raise

    @classmethod
    def safe_close(cls):
        """Safely close every async connection pool."""
        import asyncio

        for client in list(cls._async_clients.values()):
            try:
                asyncio.run(client.aclose())
            except Exception:
                pass
        cls._async_clients.clear()
        cls._instance = None


async def get_server_config() -> Optional[Dict]:
    """Fetch the server's base configuration."""
    return await ManageApiClient._instance._execute_async_request(
        "POST", "/config/server-base"
    )


async def get_agent_models(
    mac_address: str, client_id: str, selected_module: Dict
) -> Optional[Dict]:
    """Fetch the agent model configuration."""
    return await ManageApiClient._instance._execute_async_request(
        "POST",
        "/config/agent-models",
        json={
            "macAddress": mac_address,
            "clientId": client_id,
            "selectedModule": selected_module,
        },
    )


async def generate_and_save_chat_summary(session_id: str) -> Optional[Dict]:
    """Generate and save a chat history summary."""
    try:
        return await ManageApiClient._instance._execute_async_request(
            "POST",
            f"/agent/chat-summary/{session_id}/save",
        )
    except Exception as e:
        print(f"Failed to generate and save chat summary: {e}")
        return None


async def generate_and_save_chat_title(session_id: str) -> Optional[Dict]:
    """Generate and save a chat title."""
    try:
        return await ManageApiClient._instance._execute_async_request(
            "POST",
            f"/agent/chat-title/{session_id}/generate",
        )
    except Exception as e:
        print(f"Failed to generate and save chat title: {e}")
        return None


async def report(
    mac_address: str, session_id: str, chat_type: int, content: str, audio, report_time
) -> Optional[Dict]:
    """Asynchronously report a chat history entry."""
    if not content or not ManageApiClient._instance:
        return None
    try:
        return await ManageApiClient._instance._execute_async_request(
            "POST",
            f"/agent/chat-history/report",
            json={
                "macAddress": mac_address,
                "sessionId": session_id,
                "chatType": chat_type,
                "content": content,
                "reportTime": report_time,
                "audioBase64": (
                    base64.b64encode(audio).decode("utf-8") if audio else None
                ),
            },
        )
    except Exception as e:
        print(f"TTS report failed: {e}")
        return None


def init_service(config):
    ManageApiClient(config)


def manage_api_http_safe_close():
    ManageApiClient.safe_close()
