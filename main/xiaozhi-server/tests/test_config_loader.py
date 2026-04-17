"""Tests for config.config_loader.merge_configs / read_config / ensure_directories."""

from __future__ import annotations

import os
import textwrap

import pytest

from config.config_loader import (
    ensure_directories,
    get_project_dir,
    merge_configs,
    read_config,
)


class TestMergeConfigs:
    def test_returns_custom_when_both_empty(self):
        assert merge_configs({}, {}) == {}

    def test_adds_missing_keys_from_custom(self):
        merged = merge_configs({"a": 1}, {"b": 2})
        assert merged == {"a": 1, "b": 2}

    def test_custom_overrides_scalar_default(self):
        assert merge_configs({"a": 1}, {"a": 2}) == {"a": 2}

    def test_recurses_into_mappings(self):
        default = {"server": {"port": 8000, "host": "0.0.0.0"}, "debug": False}
        custom = {"server": {"port": 9000}, "debug": True}
        merged = merge_configs(default, custom)
        assert merged == {
            "server": {"port": 9000, "host": "0.0.0.0"},
            "debug": True,
        }

    def test_scalar_custom_overrides_mapping_default(self):
        assert merge_configs({"a": {"b": 1}}, {"a": "scalar"}) == {"a": "scalar"}

    def test_mapping_custom_overrides_scalar_default(self):
        assert merge_configs({"a": 1}, {"a": {"b": 2}}) == {"a": {"b": 2}}

    def test_lists_are_replaced_not_merged(self):
        # YAML-style list override semantics: custom wins entirely.
        assert merge_configs({"a": [1, 2, 3]}, {"a": [9]}) == {"a": [9]}

    def test_nested_deep_merge(self):
        default = {
            "ASR": {"FunASR": {"output_dir": "/tmp/asr", "threads": 4}},
            "LLM": {"OpenAI": {"api_key": "default"}},
        }
        custom = {
            "ASR": {"FunASR": {"threads": 8}},
            "LLM": {"OpenAI": {"api_key": "override", "model": "gpt-4"}},
        }
        merged = merge_configs(default, custom)
        assert merged == {
            "ASR": {"FunASR": {"output_dir": "/tmp/asr", "threads": 8}},
            "LLM": {"OpenAI": {"api_key": "override", "model": "gpt-4"}},
        }

    def test_original_inputs_are_not_mutated(self):
        default = {"a": {"b": 1}}
        custom = {"a": {"c": 2}}
        _ = merge_configs(default, custom)
        assert default == {"a": {"b": 1}}
        assert custom == {"a": {"c": 2}}


class TestReadConfig:
    def test_parses_yaml_file(self, tmp_path):
        path = tmp_path / "config.yaml"
        path.write_text(
            textwrap.dedent(
                """\
                server:
                  port: 8000
                  auth:
                    enabled: true
                selected_module:
                  ASR: FunASR
                """
            ),
            encoding="utf-8",
        )
        loaded = read_config(str(path))
        assert loaded["server"]["port"] == 8000
        assert loaded["server"]["auth"]["enabled"] is True
        assert loaded["selected_module"]["ASR"] == "FunASR"

    def test_missing_file_raises(self, tmp_path):
        with pytest.raises(FileNotFoundError):
            read_config(str(tmp_path / "does_not_exist.yaml"))


class TestGetProjectDir:
    def test_returns_main_xiaozhi_server_directory(self):
        project_dir = get_project_dir()
        # The helper appends a trailing slash by convention.
        assert project_dir.endswith(os.sep) or project_dir.endswith("/")
        assert os.path.isdir(project_dir)
        # config/config_loader.py lives under this directory.
        assert os.path.isfile(os.path.join(project_dir, "config", "config_loader.py"))


class TestEnsureDirectories:
    def test_creates_log_dir(self, tmp_path):
        log_dir = tmp_path / "logs"
        ensure_directories({"log": {"log_dir": str(log_dir)}})
        assert log_dir.is_dir()

    def test_creates_asr_and_tts_output_dirs(self, tmp_path):
        asr_out = tmp_path / "asr-out"
        tts_out = tmp_path / "tts-out"
        ensure_directories(
            {
                "log": {"log_dir": str(tmp_path / "logs")},
                "ASR": {"FunASR": {"output_dir": str(asr_out)}},
                "TTS": {"EdgeTTS": {"output_dir": str(tts_out)}},
                "selected_module": {"ASR": "FunASR", "TTS": "EdgeTTS"},
            }
        )
        assert asr_out.is_dir()
        assert tts_out.is_dir()

    def test_skips_modules_without_output_dir(self, tmp_path):
        # Must not raise when providers don't declare an output_dir.
        ensure_directories(
            {
                "log": {"log_dir": str(tmp_path / "logs")},
                "ASR": {"FunASR": {}},
                "TTS": {"EdgeTTS": {}},
                "selected_module": {},
            }
        )
        assert (tmp_path / "logs").is_dir()
