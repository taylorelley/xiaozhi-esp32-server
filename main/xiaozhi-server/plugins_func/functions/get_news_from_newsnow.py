import random
import requests
import json
from config.logger import setup_logging
from plugins_func.register import register_function, ToolType, ActionResponse, Action
from markitdown import MarkItDown
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler


TAG = __name__
logger = setup_logging()

CHANNEL_MAP = {
    "V2EX": "v2ex-share",
    "知乎": "zhihu",
    "微博": "weibo",
    "联合早报": "zaobao",
    "酷安": "coolapk",
    "MKTNews": "mktnews-flash",
    "华尔街见闻": "wallstreetcn-quick",
    "36氪": "36kr-quick",
    "抖音": "douyin",
    "虎扑": "hupu",
    "百度贴吧": "tieba",
    "今日头条": "toutiao",
    "IT之家": "ithome",
    "澎湃新闻": "thepaper",
    "卫星通讯社": "sputniknewscn",
    "参考消息": "cankaoxiaoxi",
    "远景论坛": "pcbeta-windows11",
    "财联社": "cls-depth",
    "雪球": "xueqiu-hotstock",
    "格隆汇": "gelonghui",
    "法布财经": "fastbull-express",
    "Solidot": "solidot",
    "Hacker News": "hackernews",
    "Product Hunt": "producthunt",
    "Github": "github-trending-today",
    "哔哩哔哩": "bilibili-hot-search",
    "快手": "kuaishou",
    "靠谱新闻": "kaopu",
    "金十数据": "jin10",
    "百度热搜": "baidu",
    "牛客": "nowcoder",
    "少数派": "sspai",
    "稀土掘金": "juejin",
    "凤凰网": "ifeng",
    "虫部落": "chongbuluo-latest",
}


# Default news sources dictionary, used when not specified in the config
DEFAULT_NEWS_SOURCES = "澎湃新闻;百度热搜;财联社"


def get_news_sources_from_config(conn):
    """Get the news sources string from the config."""
    try:
        # Try to get the news sources from the plugin config
        if (
            conn.config.get("plugins")
            and conn.config["plugins"].get("get_news_from_newsnow")
            and conn.config["plugins"]["get_news_from_newsnow"].get("news_sources")
        ):
            # Get the configured news sources string
            news_sources_config = conn.config["plugins"]["get_news_from_newsnow"][
                "news_sources"
            ]

            if isinstance(news_sources_config, str) and news_sources_config.strip():
                logger.bind(tag=TAG).debug(f"Using configured news sources: {news_sources_config}")
                return news_sources_config
            else:
                logger.bind(tag=TAG).warning("News sources config is empty or malformed, using default config")
        else:
            logger.bind(tag=TAG).debug("News sources config not found, using default config")

        return DEFAULT_NEWS_SOURCES

    except Exception as e:
        logger.bind(tag=TAG).error(f"Failed to load news sources config: {e}, using default config")
        return DEFAULT_NEWS_SOURCES


# Get all available news source names from CHANNEL_MAP
available_sources = list(CHANNEL_MAP.keys())
example_sources_str = "、".join(available_sources)

GET_NEWS_FROM_NEWSNOW_FUNCTION_DESC = {
    "type": "function",
    "function": {
        "name": "get_news_from_newsnow",
        "description": (
            "Get the latest news and randomly pick one to broadcast. "
            f"The user can choose different news sources; the standard names are: {example_sources_str}. "
            "For example, if the user asks for Baidu News, that maps to Baidu Hot Search (百度热搜). "
            "If not specified, news is fetched from The Paper (澎湃新闻) by default. "
            "The user may request the detailed content, in which case the full article is fetched."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "source": {
                    "type": "string",
                    "description": f"The standard Chinese name of the news source, for example {example_sources_str}, etc. Optional; if not provided, the default source is used.",
                },
                "detail": {
                    "type": "boolean",
                    "description": "Whether to fetch detailed content. Default is false. If true, retrieves the full content of the previous news item.",
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


def fetch_news_from_api(conn: "ConnectionHandler", source="thepaper"):
    """Fetch the news list from the API."""
    try:
        api_url = f"https://newsnow.busiyi.world/api/s?id={source}"

        news_config = conn.config.get("plugins", {}).get("get_news_from_newsnow", {})
        if news_config.get("url"):
            api_url = news_config["url"] + source

        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(api_url, headers=headers, timeout=10)
        response.raise_for_status()

        data = response.json()

        if "items" in data:
            return data["items"]
        else:
            logger.bind(tag=TAG).error(f"News API response format error: {data}")
            return []

    except Exception as e:
        logger.bind(tag=TAG).error(f"Failed to fetch news from API: {e}")
        return []


def fetch_news_detail(url):
    """Fetch the news detail page content and clean the HTML using MarkItDown."""
    try:
        headers = {"User-Agent": "Mozilla/5.0"}
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()

        # Use MarkItDown to clean the HTML content
        md = MarkItDown(enable_plugins=False)
        result = md.convert(response)

        # Get the cleaned text content
        clean_text = result.text_content

        # If the cleaned content is empty, return a notice
        if not clean_text or len(clean_text.strip()) == 0:
            logger.bind(tag=TAG).warning(f"Cleaned news content is empty: {url}")
            return "Unable to parse the news detail content; the site structure may be unusual or the content may be restricted."

        return clean_text
    except Exception as e:
        logger.bind(tag=TAG).error(f"Failed to fetch news detail: {e}")
        return "Unable to fetch detailed content"


@register_function(
    "get_news_from_newsnow",
    GET_NEWS_FROM_NEWSNOW_FUNCTION_DESC,
    ToolType.SYSTEM_CTL,
)
def get_news_from_newsnow(
    conn: "ConnectionHandler",
    source: str = "澎湃新闻",
    detail: bool = False,
    lang: str = "zh_CN",
):
    """Fetch news and randomly pick one to broadcast, or fetch the detailed content of the previous news item."""
    try:
        # Get the currently configured news sources
        news_sources = get_news_sources_from_config(conn)

        # If detail is True, fetch the detailed content of the previous news item
        detail = str(detail).lower() == "true"
        if detail:
            if (
                not hasattr(conn, "last_newsnow_link")
                or not conn.last_newsnow_link
                or "url" not in conn.last_newsnow_link
            ):
                return ActionResponse(
                    Action.REQLLM,
                    "Sorry, no recently queried news was found. Please fetch a news item first.",
                    None,
                )

            url = conn.last_newsnow_link.get("url")
            title = conn.last_newsnow_link.get("title", "Unknown title")
            source_id = conn.last_newsnow_link.get("source_id", "thepaper")
            source_name = CHANNEL_MAP.get(source_id, "Unknown source")

            if not url or url == "#":
                return ActionResponse(
                    Action.REQLLM, "Sorry, this news item has no available link to fetch details.", None
                )

            logger.bind(tag=TAG).debug(
                f"Fetching news detail: {title}, source: {source_name}, URL={url}"
            )

            # Fetch the news detail
            detail_content = fetch_news_detail(url)

            if not detail_content or detail_content == "Unable to fetch detailed content":
                return ActionResponse(
                    Action.REQLLM,
                    f"Sorry, unable to fetch the detailed content of \"{title}\". The link may have expired or the site structure may have changed.",
                    None,
                )

            # Build the detail report
            detail_report = (
                f"Based on the following data, respond to the user's news detail request in {lang}:\n\n"
                f"News title: {title}\n"
                # f"News source: {source_name}\n"
                f"Detailed content: {detail_content}\n\n"
                f"(Please summarize the news content above, extract the key information, and report it to the user in a natural and fluent way. "
                f"Do not mention that this is a summary; simply tell the story as if narrating a complete news piece.)"
            )

            return ActionResponse(Action.REQLLM, detail_report, None)

        # Otherwise, fetch the news list and pick one at random
        # Convert the Chinese name to the English ID
        english_source_id = None

        # Check whether the input Chinese name is among the configured news sources
        news_sources_list = [
            name.strip() for name in news_sources.split(";") if name.strip()
        ]
        if source in news_sources_list:
            # If the input Chinese name is among the configured sources, look up the English ID in CHANNEL_MAP
            english_source_id = CHANNEL_MAP.get(source)

        # If no matching English ID is found, fall back to the default source
        if not english_source_id:
            logger.bind(tag=TAG).warning(f"Invalid news source: {source}, falling back to the default source The Paper (澎湃新闻)")
            english_source_id = "thepaper"
            source = "澎湃新闻"

        logger.bind(tag=TAG).info(f"Fetching news: source={source}({english_source_id})")

        # Fetch the news list
        news_items = fetch_news_from_api(conn, english_source_id)

        if not news_items:
            return ActionResponse(
                Action.REQLLM,
                f"Sorry, unable to fetch news from {source}. Please try again later or try another news source.",
                None,
            )

        # Randomly pick one news item
        selected_news = random.choice(news_items)

        # Save the current news link to the connection object so details can be queried later
        if not hasattr(conn, "last_newsnow_link"):
            conn.last_newsnow_link = {}
        conn.last_newsnow_link = {
            "url": selected_news.get("url", "#"),
            "title": selected_news.get("title", "Unknown title"),
            "source_id": english_source_id,
        }

        # Build the news report
        news_report = (
            f"Based on the following data, respond to the user's news query in {lang}:\n\n"
            f"News title: {selected_news['title']}\n"
            # f"News source: {source}\n"
            f"(Please broadcast this news headline to the user in a natural and fluent way, "
            f"and hint to the user that they can ask for the detailed content, at which point the full article will be fetched.)"
        )

        return ActionResponse(Action.REQLLM, news_report, None)

    except Exception as e:
        logger.bind(tag=TAG).error(f"Error fetching news: {e}")
        return ActionResponse(
            Action.REQLLM, "Sorry, an error occurred while fetching the news. Please try again later.", None
        )
