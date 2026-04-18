"""Tests for core.auth.AuthManager (HMAC-SHA256 signer)."""

from __future__ import annotations

import time

import pytest

from core.auth import AuthenticationError, AuthManager


@pytest.fixture()
def mgr() -> AuthManager:
    return AuthManager(secret_key="unit-test-secret", expire_seconds=60)


class TestGenerateAndVerify:
    def test_generate_returns_non_empty_string(self, mgr: AuthManager):
        token = mgr.generate_token("client-1", "user-1")
        assert isinstance(token, str)
        assert len(token) > 10
        # Token layout is <sig>.<ts> with exactly one dot.
        assert token.count(".") == 1

    def test_roundtrip_verify_succeeds(self, mgr: AuthManager):
        token = mgr.generate_token("client-1", "user-1")
        assert mgr.verify_token(token, "client-1", "user-1") is True

    def test_verify_rejects_wrong_client(self, mgr: AuthManager):
        token = mgr.generate_token("client-1", "user-1")
        assert mgr.verify_token(token, "client-2", "user-1") is False

    def test_verify_rejects_wrong_username(self, mgr: AuthManager):
        token = mgr.generate_token("client-1", "user-1")
        assert mgr.verify_token(token, "client-1", "user-2") is False

    def test_verify_rejects_garbage(self, mgr: AuthManager):
        assert mgr.verify_token("not-a-token", "client-1", "user-1") is False
        assert mgr.verify_token("a.b.c", "client-1", "user-1") is False
        assert mgr.verify_token("", "client-1", "user-1") is False

    def test_verify_rejects_tampered_signature(self, mgr: AuthManager):
        token = mgr.generate_token("client-1", "user-1")
        sig, ts = token.split(".")
        tampered = (sig[:-1] + ("A" if sig[-1] != "A" else "B")) + "." + ts
        assert mgr.verify_token(tampered, "client-1", "user-1") is False

    def test_different_secrets_produce_different_tokens(self):
        a = AuthManager("secret-A").generate_token("c", "u")
        b = AuthManager("secret-B").generate_token("c", "u")
        assert a.split(".")[0] != b.split(".")[0]


class TestExpiration:
    def test_expired_token_fails_verification(self):
        mgr = AuthManager("s", expire_seconds=1)
        token = mgr.generate_token("c", "u")
        # Fast-forward by replacing the stored timestamp.
        sig, ts = token.split(".")
        stale_ts = int(ts) - 10
        stale_token = f"{sig}.{stale_ts}"
        # A different ts invalidates the signature too, but the expiration check
        # returns False first. Either way, the verifier must reject it.
        assert mgr.verify_token(stale_token, "c", "u") is False

    def test_zero_expire_uses_default_30_days(self):
        mgr = AuthManager("s", expire_seconds=0)
        assert mgr.expire_seconds == 60 * 60 * 24 * 30

    def test_negative_expire_uses_default_30_days(self):
        mgr = AuthManager("s", expire_seconds=-1)
        assert mgr.expire_seconds == 60 * 60 * 24 * 30

    def test_freshly_generated_token_is_within_window(self, mgr: AuthManager):
        before = int(time.time())
        token = mgr.generate_token("c", "u")
        _, ts = token.split(".")
        assert before <= int(ts) <= int(time.time())


class TestAuthenticationError:
    def test_authentication_error_is_exception_subclass(self):
        assert issubclass(AuthenticationError, Exception)
        with pytest.raises(AuthenticationError):
            raise AuthenticationError("nope")
