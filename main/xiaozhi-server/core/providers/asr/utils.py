import re
from config.logger import setup_logging

TAG = __name__
logger = setup_logging()

EMOTION_EMOJI_MAP = {
    "HAPPY": "🙂",
    "SAD": "😔",
    "ANGRY": "😡",
    "NEUTRAL": "😶",
    "FEARFUL": "😰",
    "DISGUSTED": "🤢",
    "SURPRISED": "😲",
    "EMO_UNKNOWN": "😶",  # Default to a neutral emoji for unknown emotions
}
# EVENT_EMOJI_MAP = {
#     "<|BGM|>": "🎼",
#     "<|Speech|>": "",
#     "<|Applause|>": "👏",
#     "<|Laughter|>": "😀",
#     "<|Cry|>": "😭",
#     "<|Sneeze|>": "🤧",
#     "<|Breath|>": "",
#     "<|Cough|>": "🤧",
# }

def lang_tag_filter(text: str) -> dict | str:
    """
    Parse a FunASR recognition result, extracting tags in order along with the plain-text content.

    Args:
        text: The raw text from ASR recognition, possibly containing multiple tags.

    Returns:
        dict: {"language": "zh", "emotion": "SAD", "emoji": "😔", "content": "hello"} when tags are present.
        str: plain text, when no tags are present.

    Examples:
        FunASR output format: <|language|><|emotion|><|event|><|other options|>original text
        >>> lang_tag_filter("<|zh|><|SAD|><|Speech|><|withitn|>Hello there, testing testing.")
        {"language": "zh", "emotion": "SAD", "emoji": "😔", "content": "Hello there, testing testing."}
        >>> lang_tag_filter("<|en|><|HAPPY|><|Speech|><|withitn|>Hello hello.")
        {"language": "en", "emotion": "HAPPY", "emoji": "🙂", "content": "Hello hello."}
        >>> lang_tag_filter("plain text")
        "plain text"
    """
    # Extract all tags (in order)
    tag_pattern = r"<\|([^|]+)\|>"
    all_tags = re.findall(tag_pattern, text)

    # Remove all <|...|> tags to get the plain text
    clean_text = re.sub(tag_pattern, "", text).strip()

    # If no tags, return plain text directly
    if not all_tags:
        return clean_text

    # Extract tags in FunASR's fixed order and return dict
    language = all_tags[0] if len(all_tags) > 0 else "zh"
    emotion = all_tags[1] if len(all_tags) > 1 else "NEUTRAL"
    # event = all_tags[2] if len(all_tags) > 2 else "Speech"  # Event tag not used for now

    result = {
        "content": clean_text,
        "language": language,
        "emotion": emotion,
        # "event": event,
    }

    # Add emoji mapping
    if emotion in EMOTION_EMOJI_MAP:
        result["emotion"] = EMOTION_EMOJI_MAP[emotion]
    # Event tag not used for now
    # if event in EVENT_EMOJI_MAP:
    #     result["event"] = EVENT_EMOJI_MAP[event]

    return result

