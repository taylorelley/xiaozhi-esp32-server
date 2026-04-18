"""Tests for core.utils.current_time."""

from __future__ import annotations

import re

from core.utils import current_time as ct

TIME_RE = re.compile(r"^\d{2}:\d{2}$")
DATE_RE = re.compile(r"^\d{4}-\d{2}-\d{2}$")


class TestFormatters:
    def test_get_current_time_format(self):
        assert TIME_RE.match(ct.get_current_time())

    def test_get_current_date_format(self):
        assert DATE_RE.match(ct.get_current_date())

    def test_get_current_weekday_known(self):
        assert ct.get_current_weekday() in {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday",
        }

    def test_get_current_lunar_date_is_string(self):
        out = ct.get_current_lunar_date()
        assert isinstance(out, str)
        assert len(out) > 0


class TestTimeInfoTuple:
    def test_returns_four_element_tuple(self):
        info = ct.get_current_time_info()
        assert isinstance(info, tuple)
        assert len(info) == 4

    def test_elements_match_individual_getters(self):
        cur, date, weekday, lunar = ct.get_current_time_info()
        assert TIME_RE.match(cur)
        assert DATE_RE.match(date)
        assert weekday in {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday",
        }
        assert isinstance(lunar, str)
