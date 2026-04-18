"""Tests for core.utils.auth.AuthToken (AES-GCM wrapped inside a JWT)."""

from __future__ import annotations

import time

import pytest

from core.utils.auth import AuthToken


@pytest.fixture()
def token_svc() -> AuthToken:
    return AuthToken("unit-test-secret")


class TestAuthTokenRoundtrip:
    def test_generate_returns_jwt_with_three_segments(self, token_svc: AuthToken):
        token = token_svc.generate_token("device-42")
        assert isinstance(token, str)
        assert token.count(".") == 2  # header.payload.signature

    def test_roundtrip_yields_original_device_id(self, token_svc: AuthToken):
        token = token_svc.generate_token("device-42")
        ok, device_id = token_svc.verify_token(token)
        assert ok is True
        assert device_id == "device-42"

    def test_same_device_id_produces_distinct_tokens(self, token_svc: AuthToken):
        # IVs must be fresh on every call, so ciphertexts (and therefore tokens) differ.
        a = token_svc.generate_token("device-42")
        b = token_svc.generate_token("device-42")
        assert a != b


class TestAuthTokenRejection:
    def test_rejects_garbage_token(self, token_svc: AuthToken):
        ok, device_id = token_svc.verify_token("not-a-jwt")
        assert ok is False
        assert device_id is None

    def test_rejects_wrong_secret(self):
        signed = AuthToken("secret-A").generate_token("device-42")
        ok, device_id = AuthToken("secret-B").verify_token(signed)
        assert ok is False
        assert device_id is None

    def test_rejects_empty_token(self, token_svc: AuthToken):
        ok, device_id = token_svc.verify_token("")
        assert ok is False
        assert device_id is None

    def test_rejects_tampered_payload(self, token_svc: AuthToken):
        token = token_svc.generate_token("device-42")
        # Flip one byte in the signature segment.
        header, payload, sig = token.split(".")
        tampered = f"{header}.{payload}.{sig[:-1]}{'A' if sig[-1] != 'A' else 'B'}"
        ok, device_id = token_svc.verify_token(tampered)
        assert ok is False
        assert device_id is None


class TestAuthTokenExpiration:
    def test_manual_expiration_rejected(self, token_svc: AuthToken):
        # Construct a token with an exp in the past by reaching into the helpers.
        expired_payload = {"device_id": "device-42", "exp": time.time() - 10}
        encrypted = token_svc._encrypt_payload(expired_payload)

        import jwt

        expired_token = jwt.encode(
            {"data": encrypted}, "unit-test-secret", algorithm="HS256"
        )
        ok, device_id = token_svc.verify_token(expired_token)
        assert ok is False
        assert device_id is None


class TestKeyDerivation:
    def test_same_secret_yields_same_key(self):
        a = AuthToken("the-secret")
        b = AuthToken("the-secret")
        assert a.encryption_key == b.encryption_key
        assert len(a.encryption_key) == 32  # AES-256 key length

    def test_different_secrets_yield_different_keys(self):
        a = AuthToken("secret-A")
        b = AuthToken("secret-B")
        assert a.encryption_key != b.encryption_key
