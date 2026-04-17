import random
import requests
import xml.etree.ElementTree as ET
from bs4 import BeautifulSoup
from config.logger import setup_logging
from plugins_func.register import register_function, ToolType, ActionResponse, Action
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from core.connection import ConnectionHandler


TAG = __name__
logger = setup_logging()

GET_NEWS_FROM_CHINANEWS_FUNCTION_DESC = {
    "type": "function",
    "function": {
        "name": "get_news_from_chinanews",
        "description": (
            "Get the latest news and randomly pick one to broadcast. "
            "The user may specify a news category such as society news, tech news, international news, etc. "
            "If not specified, society news is broadcast by default. "
            "The user may request the detailed content, in which case the full article is fetched."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "category": {
                    "type": "string",
                    "description": "News category, for example society, tech, international. Optional; if not provided the default category is used.",
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


def fetch_news_from_rss(rss_url):
    """Fetch the news list from an RSS feed."""
    try:
        response = requests.get(rss_url)
        response.raise_for_status()

        # Parse XML
        root = ET.fromstring(response.content)

        # Find all item elements (news entries)
        news_items = []
        for item in root.findall(".//item"):
            title = (
                item.find("title").text if item.find("title") is not None else "No title"
            )
            link = item.find("link").text if item.find("link") is not None else "#"
            description = (
                item.find("description").text
                if item.find("description") is not None
                else "No description"
            )
            pubDate = (
                item.find("pubDate").text
                if item.find("pubDate") is not None
                else "Unknown time"
            )

            news_items.append(
                {
                    "title": title,
                    "link": link,
                    "description": description,
                    "pubDate": pubDate,
                }
            )

        return news_items
    except Exception as e:
        logger.bind(tag=TAG).error(f"Failed to fetch RSS news: {e}")
        return []


def fetch_news_detail(url):
    """Fetch the news detail page and summarize it."""
    try:
        response = requests.get(url)
        response.raise_for_status()

        soup = BeautifulSoup(response.content, "html.parser")

        # Try to extract the main body content (the selectors here may need to be adjusted for the actual site structure)
        content_div = soup.select_one(
            ".content_desc, .content, article, .article-content"
        )
        if content_div:
            paragraphs = content_div.find_all("p")
            content = "\n".join(
                [p.get_text().strip() for p in paragraphs if p.get_text().strip()]
            )
            return content
        else:
            # If no specific content region is found, try to fetch all paragraphs
            paragraphs = soup.find_all("p")
            content = "\n".join(
                [p.get_text().strip() for p in paragraphs if p.get_text().strip()]
            )
            return content[:2000]  # Limit the length
    except Exception as e:
        logger.bind(tag=TAG).error(f"Failed to fetch news detail: {e}")
        return "Unable to fetch detailed content"


def map_category(category_text):
    """Map a user-provided Chinese category to the category key used in the config file."""
    if not category_text:
        return None

    # Category mapping dictionary. Currently supports society, international, and finance news;
    # for more types, see the config file. Keys are Chinese inputs kept as-is because the
    # LLM and users invoke this plugin with Chinese category words.
    category_map = {
        # Society news
        "\u793e\u4f1a": "society_rss_url",
        "\u793e\u4f1a\u65b0\u95fb": "society_rss_url",
        # International news
        "\u56fd\u9645": "world_rss_url",
        "\u56fd\u9645\u65b0\u95fb": "world_rss_url",
        # Finance news
        "\u8d22\u7ecf": "finance_rss_url",
        "\u8d22\u7ecf\u65b0\u95fb": "finance_rss_url",
        "\u91d1\u878d": "finance_rss_url",
        "\u7ecf\u6d4e": "finance_rss_url",
    }

    # Lowercase and strip whitespace
    normalized_category = category_text.lower().strip()

    # Return the mapped result; if no match, return the original input
    return category_map.get(normalized_category, category_text)


@register_function(
    "get_news_from_chinanews",
    GET_NEWS_FROM_CHINANEWS_FUNCTION_DESC,
    ToolType.SYSTEM_CTL,
)
def get_news_from_chinanews(
    conn: "ConnectionHandler",
    category: str = None,
    detail: bool = False,
    lang: str = "zh_CN",
):
    """Fetch news and randomly pick one to broadcast, or fetch the detailed content of the previous news item."""
    try:
        # If detail is True, fetch the detailed content of the previous news item
        if detail:
            if (
                not hasattr(conn, "last_news_link")
                or not conn.last_news_link
                or "link" not in conn.last_news_link
            ):
                return ActionResponse(
                    Action.REQLLM,
                    "Sorry, no recently queried news was found. Please fetch a news item first.",
                    None,
                )

            link = conn.last_news_link.get("link")
            title = conn.last_news_link.get("title", "Unknown title")

            if link == "#":
                return ActionResponse(
                    Action.REQLLM, "Sorry, this news item has no available link to fetch details.", None
                )

            logger.bind(tag=TAG).debug(f"Fetching news detail: {title}, URL={link}")

            # Fetch the news detail
            detail_content = fetch_news_detail(link)

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
                f"Detailed content: {detail_content}\n\n"
                f"(Please summarize the news content above, extract the key information, and report it to the user in a natural and fluent way. "
                f"Do not mention that this is a summary; simply tell the story as if narrating a complete news piece.)"
            )

            return ActionResponse(Action.REQLLM, detail_report, None)

        # Otherwise, fetch the news list and pick one at random
        # Get the RSS URL from the config
        rss_config = conn.config.get("plugins", {}).get("get_news_from_chinanews", {})
        default_rss_url = rss_config.get(
            "default_rss_url", "https://www.chinanews.com.cn/rss/society.xml"
        )

        # Map the user-provided category to the category key used in the config
        mapped_category = map_category(category)

        # If a category is provided, try to get the corresponding URL from the config
        rss_url = default_rss_url
        if mapped_category and mapped_category in rss_config:
            rss_url = rss_config[mapped_category]

        logger.bind(tag=TAG).info(
            f"Fetching news: original category={category}, mapped category={mapped_category}, URL={rss_url}"
        )

        # Fetch the news list
        news_items = fetch_news_from_rss(rss_url)

        if not news_items:
            return ActionResponse(
                Action.REQLLM, "Sorry, unable to fetch news information. Please try again later.", None
            )

        # Randomly pick one news item
        selected_news = random.choice(news_items)

        # Save the current news link to the connection object so details can be queried later
        if not hasattr(conn, "last_news_link"):
            conn.last_news_link = {}
        conn.last_news_link = {
            "link": selected_news.get("link", "#"),
            "title": selected_news.get("title", "Unknown title"),
        }

        # Build the news report
        news_report = (
            f"Based on the following data, respond to the user's news query in {lang}:\n\n"
            f"News title: {selected_news['title']}\n"
            f"Published: {selected_news['pubDate']}\n"
            f"News content: {selected_news['description']}\n"
            f"(Please broadcast this news to the user in a natural and fluent way. You may summarize appropriately; "
            f"simply read out the news without adding extra content. "
            f"If the user asks for more details, tell them they can say 'please describe this news in detail' to get more content.)"
        )

        return ActionResponse(Action.REQLLM, news_report, None)

    except Exception as e:
        logger.bind(tag=TAG).error(f"Error fetching news: {e}")
        return ActionResponse(
            Action.REQLLM, "Sorry, an error occurred while fetching the news. Please try again later.", None
        )
