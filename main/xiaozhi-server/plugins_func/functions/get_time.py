from datetime import datetime
import cnlunar
from plugins_func.register import register_function, ToolType, ActionResponse, Action

get_lunar_function_desc = {
    "type": "function",
    "function": {
        "name": "get_lunar",
        "description": (
            "Used to obtain lunar-calendar and Chinese almanac information for a specific date. "
            "The user can ask for specific items such as the lunar date, Heavenly Stems and Earthly Branches, "
            "solar terms, zodiac animal, Western zodiac sign, BaZi (Four Pillars), things suitable or to avoid, etc. "
            "If no content is specified, the Stems-and-Branches year and lunar date are returned by default. "
            "For basic queries such as 'what is today's lunar date', use the information already present in the context; do not call this tool."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "date": {
                    "type": "string",
                    "description": "Date to query, in YYYY-MM-DD format, e.g. 2024-01-01. If omitted, the current date is used.",
                },
                "query": {
                    "type": "string",
                    "description": "Information to query, e.g. lunar date, Heavenly Stems and Earthly Branches, holidays, solar terms, zodiac animal, Western zodiac sign, BaZi, things suitable or to avoid, etc.",
                },
            },
            "required": [],
        },
    },
}


@register_function("get_lunar", get_lunar_function_desc, ToolType.WAIT)
def get_lunar(date=None, query=None):
    """
    Obtain the current lunar date along with almanac data such as Heavenly Stems and Earthly Branches,
    solar terms, zodiac animal, Western zodiac sign, BaZi, and things suitable or to avoid.
    """
    from core.utils.cache.manager import cache_manager, CacheType

    # Use the specified date if provided; otherwise use the current date
    if date:
        try:
            now = datetime.strptime(date, "%Y-%m-%d")
        except ValueError:
            return ActionResponse(
                Action.REQLLM,
                f"Invalid date format. Please use YYYY-MM-DD, for example 2024-01-01",
                None,
            )
    else:
        now = datetime.now()

    current_date = now.strftime("%Y-%m-%d")

    # If query is None, use the default text
    if query is None:
        query = "Default query: Stems-and-Branches year and lunar date"

    # Try to fetch the lunar information from the cache
    lunar_cache_key = f"lunar_info_{current_date}"
    cached_lunar_info = cache_manager.get(CacheType.LUNAR, lunar_cache_key)
    if cached_lunar_info:
        return ActionResponse(Action.REQLLM, cached_lunar_info, None)

    response_text = f"Based on the following information, respond to the user's query and provide details related to {query}:\n"

    lunar = cnlunar.Lunar(now, godType="8char")
    response_text += (
        "Lunar information:\n"
        "%s year %s%s\n" % (lunar.lunarYearCn, lunar.lunarMonthCn[:-1], lunar.lunarDayCn)
        + "Stems and Branches: %s year %s month %s day\n" % (lunar.year8Char, lunar.month8Char, lunar.day8Char)
        + "Zodiac: %s\n" % (lunar.chineseYearZodiac)
        + "BaZi: %s\n"
        % (
            " ".join(
                [lunar.year8Char, lunar.month8Char, lunar.day8Char, lunar.twohour8Char]
            )
        )
        + "Today's holidays: %s\n"
        % (
            ",".join(
                filter(
                    None,
                    (
                        lunar.get_legalHolidays(),
                        lunar.get_otherHolidays(),
                        lunar.get_otherLunarHolidays(),
                    ),
                )
            )
        )
        + "Today's solar term: %s\n" % (lunar.todaySolarTerms)
        + "Next solar term: %s, %s-%s-%s\n"
        % (
            lunar.nextSolarTerm,
            lunar.nextSolarTermYear,
            lunar.nextSolarTermDate[0],
            lunar.nextSolarTermDate[1],
        )
        + "Solar-term calendar for this year: %s\n"
        % (
            ", ".join(
                [
                    f"{term}(month {date[0]}, day {date[1]})"
                    for term, date in lunar.thisYearSolarTermsDic.items()
                ]
            )
        )
        + "Zodiac clash: %s\n" % (lunar.chineseZodiacClash)
        + "Western zodiac: %s\n" % (lunar.starZodiac)
        + "Nayin: %s\n" % lunar.get_nayin()
        + "Pengzu taboos: %s\n" % (lunar.get_pengTaboo(delimit=", "))
        + "Day officer: %s\n" % lunar.get_today12DayOfficer()[0]
        + "Day deity: %s (%s)\n"
        % (lunar.get_today12DayOfficer()[1], lunar.get_today12DayOfficer()[2])
        + "28 Mansions: %s\n" % lunar.get_the28Stars()
        + "Auspicious directions: %s\n" % " ".join(lunar.get_luckyGodsDirection())
        + "Fetal god today: %s\n" % lunar.get_fetalGod()
        + "Suitable: %s\n" % ", ".join(lunar.goodThing[:10])
        + "To avoid: %s\n" % ", ".join(lunar.badThing[:10])
        + "(By default, return the Stems-and-Branches year and lunar date; return today's suitable/to-avoid info only when explicitly requested.)"
    )

    # Cache the lunar information
    cache_manager.set(CacheType.LUNAR, lunar_cache_key, response_text)

    return ActionResponse(Action.REQLLM, response_text, None)
