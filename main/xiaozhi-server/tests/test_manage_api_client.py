"""Tests for config.manage_api_client.

We don't exercise the full request/retry loop here -- the real client opens
a real httpx.AsyncClient bound to an event loop and relies on a singleton,
which makes it hard to isolate. Instead we exercise the pieces that have
non-trivial, testable semantics:

  * DeviceBindException / DeviceNotFoundException construction
  * _should_retry's HTTP-status classification
  * init_service misconfiguration guards
  * report() no-ops when the client singleton isn't initialised
"""

from __future__ import annotations

from unittest.mock import MagicMock

import httpx
import pytest

import config.manage_api_client as mac
from config.manage_api_client import (
    DeviceBindException,
    DeviceNotFoundException,
    ManageApiClient,
    init_service,
    report,
)


@pytest.fixture(autouse=True)
def _reset_singleton():
    """Ensure a clean slate for ManageApiClient's class-level singleton state."""
    ManageApiClient._instance = None
    ManageApiClient._async_clients = {}
    ManageApiClient._secret = None
    yield
    ManageApiClient._instance = None
    ManageApiClient._async_clients = {}
    ManageApiClient._secret = None


class TestCustomExceptions:
    def test_device_not_found_is_exception(self):
        assert issubclass(DeviceNotFoundException, Exception)
        with pytest.raises(DeviceNotFoundException):
            raise DeviceNotFoundException("nope")

    def test_device_bind_stores_code_and_message(self):
        err = DeviceBindException("ABC123")
        assert err.bind_code == "ABC123"
        assert "ABC123" in str(err)


class TestShouldRetry:
    @pytest.mark.parametrize(
        "exc",
        [
            httpx.ConnectError("boom"),
            httpx.TimeoutException("slow"),
            httpx.NetworkError("down"),
        ],
    )
    def test_network_errors_are_retryable(self, exc):
        assert ManageApiClient._should_retry(exc) is True

    @pytest.mark.parametrize("status_code", [408, 429, 500, 502, 503, 504])
    def test_transient_http_statuses_are_retryable(self, status_code):
        response = MagicMock(spec=httpx.Response)
        response.status_code = status_code
        err = httpx.HTTPStatusError("bad", request=MagicMock(), response=response)
        assert ManageApiClient._should_retry(err) is True

    @pytest.mark.parametrize("status_code", [400, 401, 403, 404, 422])
    def test_client_errors_are_not_retryable(self, status_code):
        response = MagicMock(spec=httpx.Response)
        response.status_code = status_code
        err = httpx.HTTPStatusError("bad", request=MagicMock(), response=response)
        assert ManageApiClient._should_retry(err) is False

    def test_unknown_exceptions_are_not_retryable(self):
        assert ManageApiClient._should_retry(ValueError("nope")) is False


class TestInitServiceGuards:
    def test_missing_manager_api_block_raises(self):
        with pytest.raises(Exception, match="manager-api configuration"):
            init_service({})

    def test_placeholder_secret_raises(self):
        with pytest.raises(Exception, match="configure the manager-api secret"):
            init_service(
                {"manager-api": {"url": "http://api.example.test", "secret": "your-secret-here"}}
            )

    def test_valid_config_initialises_singleton(self):
        init_service(
            {"manager-api": {"url": "http://api.example.test", "secret": "real-secret"}}
        )
        assert ManageApiClient._instance is not None
        assert ManageApiClient._secret == "real-secret"


class TestReportShortCircuit:
    async def test_report_returns_none_without_instance(self):
        # When the singleton isn't set up, report() must silently no-op.
        result = await report("mac", "session", 1, "hello", None, "now")
        assert result is None

    async def test_report_returns_none_for_empty_content(self):
        init_service(
            {"manager-api": {"url": "http://api.example.test", "secret": "real-secret"}}
        )
        result = await report("mac", "session", 1, "", None, "now")
        assert result is None


# Sanity check: the module uses httpx under the hood, make sure the import
# resolves and the module object exposes the symbols we rely on elsewhere.
def test_module_surface():
    for name in (
        "ManageApiClient",
        "DeviceNotFoundException",
        "DeviceBindException",
        "init_service",
        "manage_api_http_safe_close",
        "report",
        "get_server_config",
        "get_agent_models",
    ):
        assert hasattr(mac, name), f"missing export: {name}"
