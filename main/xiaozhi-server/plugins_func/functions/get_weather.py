import requests
from bs4 import BeautifulSoup
from config.logger import setup_logging
from plugins_func.register import register_function, ToolType, ActionResponse, Action
from core.utils.util import get_ip_info
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler

TAG = __name__
logger = setup_logging()

GET_WEATHER_FUNCTION_DESC = {
    "type": "function",
    "function": {
        "name": "get_weather",
        "description": (
            "Get the weather for a given location. The user should provide a location; "
            "for example, if the user says 'Hangzhou weather', the parameter is: Hangzhou. "
            "If the user names a province, default to its provincial capital. "
            "If the user names a place that is neither a province nor a city, default to the provincial capital of the province that place belongs to. "
            "If the user does not specify a location and says things like 'how's the weather' or 'what's the weather like today', leave the location parameter empty."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "location": {
                    "type": "string",
                    "description": "Location name, for example Hangzhou. Optional; if not provided, do not pass it.",
                },
                "lang": {
                    "type": "string",
                    "description": "Language code used by the user for the response, e.g. zh_CN/zh_HK/en_US/ja_JP, defaults to zh_CN.",
                },
            },
            "required": ["lang"],
        },
    },
}

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36"
    )
}

# Weather codes https://dev.qweather.com/docs/resource/icons/#weather-icons
WEATHER_CODE_MAP = {
    "100": "Sunny",
    "101": "Cloudy",
    "102": "Partly cloudy",
    "103": "Sunny with clouds",
    "104": "Overcast",
    "150": "Sunny",
    "151": "Cloudy",
    "152": "Partly cloudy",
    "153": "Sunny with clouds",
    "300": "Shower",
    "301": "Heavy shower",
    "302": "Thunderstorm",
    "303": "Heavy thunderstorm",
    "304": "Thunderstorm with hail",
    "305": "Light rain",
    "306": "Moderate rain",
    "307": "Heavy rain",
    "308": "Extreme rainfall",
    "309": "Drizzle/light rain",
    "310": "Rainstorm",
    "311": "Heavy rainstorm",
    "312": "Severe rainstorm",
    "313": "Freezing rain",
    "314": "Light to moderate rain",
    "315": "Moderate to heavy rain",
    "316": "Heavy rain to rainstorm",
    "317": "Rainstorm to heavy rainstorm",
    "318": "Heavy rainstorm to severe rainstorm",
    "350": "Shower",
    "351": "Heavy shower",
    "399": "Rain",
    "400": "Light snow",
    "401": "Moderate snow",
    "402": "Heavy snow",
    "403": "Blizzard",
    "404": "Sleet",
    "405": "Rain and snow",
    "406": "Shower sleet",
    "407": "Snow shower",
    "408": "Light to moderate snow",
    "409": "Moderate to heavy snow",
    "410": "Heavy snow to blizzard",
    "456": "Shower sleet",
    "457": "Snow shower",
    "499": "Snow",
    "500": "Mist",
    "501": "Fog",
    "502": "Haze",
    "503": "Blowing sand",
    "504": "Floating dust",
    "507": "Sandstorm",
    "508": "Severe sandstorm",
    "509": "Dense fog",
    "510": "Very dense fog",
    "511": "Moderate haze",
    "512": "Heavy haze",
    "513": "Severe haze",
    "514": "Heavy fog",
    "515": "Very heavy fog",
    "900": "Hot",
    "901": "Cold",
    "999": "Unknown",
}


def fetch_city_info(location, api_key, api_host):
    # lang=zh is preserved as an API parameter for the QWeather geo lookup API.
    url = f"https://{api_host}/geo/v2/city/lookup?key={api_key}&location={location}&lang=zh"
    response = requests.get(url, headers=HEADERS).json()
    if response.get("error") is not None:
        logger.bind(tag=TAG).error(
            f"Failed to fetch weather, reason: {response.get('error', {}).get('detail')}"
        )
        return None
    return response.get("location", [])[0] if response.get("location") else None


def fetch_weather_page(url):
    response = requests.get(url, headers=HEADERS)
    return BeautifulSoup(response.text, "html.parser") if response.ok else None


def parse_weather_info(soup):
    city_name = soup.select_one("h1.c-submenu__location").get_text(strip=True)

    current_abstract = soup.select_one(".c-city-weather-current .current-abstract")
    current_abstract = (
        current_abstract.get_text(strip=True) if current_abstract else "Unknown"
    )

    current_basic = {}
    for item in soup.select(
        ".c-city-weather-current .current-basic .current-basic___item"
    ):
        parts = item.get_text(strip=True, separator=" ").split(" ")
        if len(parts) == 2:
            key, value = parts[1], parts[0]
            current_basic[key] = value

    temps_list = []
    for row in soup.select(".city-forecast-tabs__row")[:7]:  # Take the first 7 days of data
        date = row.select_one(".date-bg .date").get_text(strip=True)
        weather_code = (
            row.select_one(".date-bg .icon")["src"].split("/")[-1].split(".")[0]
        )
        weather = WEATHER_CODE_MAP.get(weather_code, "Unknown")
        temps = [span.get_text(strip=True) for span in row.select(".tmp-cont .temp")]
        high_temp, low_temp = (temps[0], temps[-1]) if len(temps) >= 2 else (None, None)
        temps_list.append((date, weather, high_temp, low_temp))

    return city_name, current_abstract, current_basic, temps_list


@register_function("get_weather", GET_WEATHER_FUNCTION_DESC, ToolType.SYSTEM_CTL)
def get_weather(conn: "ConnectionHandler", location: str = None, lang: str = "zh_CN"):
    from core.utils.cache.manager import cache_manager, CacheType

    weather_config = conn.config.get("plugins", {}).get("get_weather", {})
    api_host = weather_config.get("api_host", "mj7p3y7naa.re.qweatherapi.com")
    api_key = weather_config.get("api_key", "a861d0d5e7bf4ee1a83d9a9e4f96d4da")
    # Default to Guangzhou when the user does not provide a location.
    # The fallback is a Chinese city name since it is passed to the QWeather
    # geo lookup API, which expects location names in Chinese.
    default_location = weather_config.get("default_location", "\u5e7f\u5dde")
    client_ip = conn.client_ip

    # Prefer the location parameter provided by the user
    if not location:
        # Resolve the city from the client IP
        if client_ip:
            # First try the cache for the city info corresponding to the IP
            cached_ip_info = cache_manager.get(CacheType.IP_INFO, client_ip)
            if cached_ip_info:
                location = cached_ip_info.get("city")
            else:
                # Cache miss: call the API to fetch it
                ip_info = get_ip_info(client_ip, logger)
                if ip_info:
                    cache_manager.set(CacheType.IP_INFO, client_ip, ip_info)
                    location = ip_info.get("city")

            if not location:
                location = default_location
        else:
            # If there is no IP, use the default location
            location = default_location
    # Try to get the full weather report from the cache
    weather_cache_key = f"full_weather_{location}_{lang}"
    cached_weather_report = cache_manager.get(CacheType.WEATHER, weather_cache_key)
    if cached_weather_report:
        return ActionResponse(Action.REQLLM, cached_weather_report, None)

    # Cache miss: fetch real-time weather data
    city_info = fetch_city_info(location, api_key, api_host)
    if not city_info:
        return ActionResponse(
            Action.REQLLM, f"City not found: {location}. Please check that the location is correct.", None
        )
    soup = fetch_weather_page(city_info["fxLink"])
    if not soup:
        return ActionResponse(Action.REQLLM, None, "Request failed")
    city_name, current_abstract, current_basic, temps_list = parse_weather_info(soup)

    weather_report = f"The location you queried is: {city_name}\n\nCurrent weather: {current_abstract}\n"

    # Add valid current weather parameters
    if current_basic:
        weather_report += "Detailed parameters:\n"
        for key, value in current_basic.items():
            if value != "0":  # Filter out invalid values
                weather_report += f"  - {key}: {value}\n"

    # Add the 7-day forecast
    weather_report += "\n7-day forecast:\n"
    for date, weather, high, low in temps_list:
        weather_report += f"{date}: {weather}, temperature {low}~{high}\n"

    # Hint message
    weather_report += "\n(If you want the weather for a specific day, please tell me the date.)"

    # Cache the full weather report
    cache_manager.set(CacheType.WEATHER, weather_cache_key, weather_report)

    return ActionResponse(Action.REQLLM, weather_report, None)
