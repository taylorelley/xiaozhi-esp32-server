"""Tests for core.utils.output_counter."""

from __future__ import annotations

import datetime

import pytest

import core.utils.output_counter as oc


@pytest.fixture(autouse=True)
def _reset_counter_state():
    """The module keeps process-global state; wipe it before and after each test."""
    oc.reset_device_output()
    oc._last_check_date = None
    yield
    oc.reset_device_output()
    oc._last_check_date = None


class TestOutputCounter:
    def test_unseen_device_reports_zero(self):
        assert oc.get_device_output("dev-a") == 0

    def test_adding_output_accumulates(self):
        oc.add_device_output("dev-a", 100)
        oc.add_device_output("dev-a", 50)
        assert oc.get_device_output("dev-a") == 150

    def test_add_isolates_per_device(self):
        oc.add_device_output("dev-a", 42)
        oc.add_device_output("dev-b", 7)
        assert oc.get_device_output("dev-a") == 42
        assert oc.get_device_output("dev-b") == 7

    def test_check_limit_returns_true_when_exceeded(self):
        oc.add_device_output("dev-a", 120)
        assert oc.check_device_output_limit("dev-a", 100) is True

    def test_check_limit_returns_false_when_under(self):
        oc.add_device_output("dev-a", 50)
        assert oc.check_device_output_limit("dev-a", 100) is False

    def test_check_limit_returns_true_when_exactly_at_limit(self):
        oc.add_device_output("dev-a", 100)
        # The implementation uses >=, so hitting the limit counts as exceeding it.
        assert oc.check_device_output_limit("dev-a", 100) is True

    def test_check_limit_returns_false_for_empty_device_id(self):
        oc.add_device_output("", 500)  # This gets stored, but the checker bails early.
        assert oc.check_device_output_limit("", 1) is False

    def test_reset_clears_all_counters(self):
        oc.add_device_output("dev-a", 100)
        oc.add_device_output("dev-b", 200)
        oc.reset_device_output()
        assert oc.get_device_output("dev-a") == 0
        assert oc.get_device_output("dev-b") == 0


class TestDayRollover:
    def test_rollover_resets_on_new_day(self, monkeypatch):
        oc.add_device_output("dev-a", 300)
        assert oc.get_device_output("dev-a") == 300

        # Force the counter to think it is now a different day.
        fake_today = datetime.date.today() + datetime.timedelta(days=1)

        class _FrozenDatetime(datetime.datetime):
            @classmethod
            def now(cls, tz=None):
                base = super().now(tz)
                return base.replace(year=fake_today.year, month=fake_today.month, day=fake_today.day)

        monkeypatch.setattr(oc.datetime, "datetime", _FrozenDatetime)

        oc.add_device_output("dev-a", 10)
        assert oc.get_device_output("dev-a") == 10
