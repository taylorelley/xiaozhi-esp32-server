"""Tests for core.utils.textUtils."""

from __future__ import annotations

import pytest

from core.utils.textUtils import (
    EMOJI_MAP,
    check_emoji,
    get_string_no_punctuation_or_emoji,
    is_emoji,
    is_punctuation_or_emoji,
)


class TestIsEmoji:
    @pytest.mark.parametrize("char", list(EMOJI_MAP.keys()))
    def test_all_mapped_emojis_are_classified_as_emojis(self, char):
        assert is_emoji(char) is True

    @pytest.mark.parametrize("char", ["a", "1", ".", " ", "!", "\u4e2d"])  # incl. a CJK ideograph
    def test_non_emoji_chars(self, char):
        assert is_emoji(char) is False


class TestIsPunctuationOrEmoji:
    @pytest.mark.parametrize("char", [",", ".", "!", ":", "。", "，", "【", "】", "-", "\t", " ", "\n"])
    def test_punctuation_and_whitespace(self, char):
        assert is_punctuation_or_emoji(char) is True

    def test_emojis_are_included(self):
        assert is_punctuation_or_emoji("\U0001f642") is True  # 🙂

    def test_letters_are_not(self):
        assert is_punctuation_or_emoji("a") is False
        assert is_punctuation_or_emoji("\u4e2d") is False


class TestGetStringNoPunctuationOrEmoji:
    def test_trims_leading_and_trailing_whitespace(self):
        assert get_string_no_punctuation_or_emoji("  hello  ") == "hello"

    def test_trims_leading_and_trailing_punctuation(self):
        assert get_string_no_punctuation_or_emoji("。hello！") == "hello"

    def test_keeps_internal_punctuation(self):
        # Comma+space are stripped at the edges only.
        assert get_string_no_punctuation_or_emoji("  hello, world!  ") == "hello, world"

    def test_strips_trailing_emoji(self):
        assert get_string_no_punctuation_or_emoji("happy \U0001f642") == "happy"

    def test_returns_empty_for_all_punctuation(self):
        assert get_string_no_punctuation_or_emoji("  ,. 。！ ") == ""

    def test_empty_input(self):
        assert get_string_no_punctuation_or_emoji("") == ""


class TestCheckEmoji:
    def test_removes_inline_emoji(self):
        assert check_emoji("hello \U0001f602 world") == "hello  world"

    def test_removes_newline_characters(self):
        assert check_emoji("line1\nline2") == "line1line2"

    def test_leaves_non_emoji_text_alone(self):
        assert check_emoji("nothing to remove") == "nothing to remove"

    def test_strips_multiple_emojis(self):
        assert check_emoji("a\U0001f602b\U0001f614c") == "abc"
