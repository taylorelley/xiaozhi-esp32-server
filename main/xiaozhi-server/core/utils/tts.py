import os
import re
import sys
import importlib

from config.logger import setup_logging
from core.utils.textUtils import check_emoji

logger = setup_logging()

punctuation_set = {
    "，",
    ",",  # Chinese comma + English comma
    "。",
    ".",  # Chinese period + English period
    "！",
    "!",  # Chinese exclamation mark + English exclamation mark
    "“",
    "”",
    '"',  # Chinese double quotes + English double quote
    "：",
    ":",  # Chinese colon + English colon
    "-",
    "－",  # English hyphen + Chinese full-width dash
    "、",  # Chinese enumeration comma
    "[",
    "]",  # Square brackets
    "【",
    "】",  # Chinese square brackets
    "~",  # Tilde
}

def create_instance(class_name, *args, **kwargs):
    # Create a TTS instance
    if os.path.exists(os.path.join('core', 'providers', 'tts', f'{class_name}.py')):
        lib_name = f'core.providers.tts.{class_name}'
        if lib_name not in sys.modules:
            sys.modules[lib_name] = importlib.import_module(f'{lib_name}')
        return sys.modules[lib_name].TTSProvider(*args, **kwargs)

    raise ValueError(f"Unsupported TTS type: {class_name}. Please verify the 'type' field in the configuration is correct")


class MarkdownCleaner:
    """
    Encapsulates Markdown cleanup logic. Use MarkdownCleaner.clean_markdown(text) directly.
    """
    # Formula characters
    NORMAL_FORMULA_CHARS = re.compile(r'[a-zA-Z\\^_{}\+\-\(\)\[\]=]')

    @staticmethod
    def _replace_inline_dollar(m: re.Match) -> str:
        """
        Whenever a full "$...$" is captured:
          - If the inside contains typical formula characters, drop the surrounding $.
          - Otherwise (pure digits, currency, etc.), keep "$...$" as is.
        """
        content = m.group(1)
        if MarkdownCleaner.NORMAL_FORMULA_CHARS.search(content):
            return content
        else:
            return m.group(0)

    @staticmethod
    def _replace_table_block(match: re.Match) -> str:
        """
        Callback invoked when an entire Markdown table block is matched.
        """
        block_text = match.group('table_block')
        lines = block_text.strip('\n').split('\n')

        parsed_table = []
        for line in lines:
            line_stripped = line.strip()
            if re.match(r'^\|\s*[-:]+\s*(\|\s*[-:]+\s*)+\|?$', line_stripped):
                continue
            columns = [col.strip() for col in line_stripped.split('|') if col.strip() != '']
            if columns:
                parsed_table.append(columns)

        if not parsed_table:
            return ""

        headers = parsed_table[0]
        data_rows = parsed_table[1:] if len(parsed_table) > 1 else []

        lines_for_tts = []
        if len(parsed_table) == 1:
            # Only one row
            only_line_str = ", ".join(parsed_table[0])
            lines_for_tts.append(f"Single-row table: {only_line_str}")
        else:
            lines_for_tts.append(f"Header: {', '.join(headers)}")
            for i, row in enumerate(data_rows, start=1):
                row_str_list = []
                for col_index, cell_val in enumerate(row):
                    if col_index < len(headers):
                        row_str_list.append(f"{headers[col_index]} = {cell_val}")
                    else:
                        row_str_list.append(cell_val)
                lines_for_tts.append(f"Row {i}: {', '.join(row_str_list)}")

        return "\n".join(lines_for_tts) + "\n"

    # Pre-compile all regular expressions (ordered by how often they run).
    # The replace_xxx static methods must be defined above so that they can be
    # referenced correctly in this list.
    REGEXES = [
        (re.compile(r'```.*?```', re.DOTALL), ''),  # Code block
        (re.compile(r'^#+\s*', re.MULTILINE), ''),  # Heading
        (re.compile(r'(\*\*|__)(.*?)\1'), r'\2'),  # Bold
        (re.compile(r'(\*|_)(?=\S)(.*?)(?<=\S)\1'), r'\2'),  # Italic
        (re.compile(r'!\[.*?\]\(.*?\)'), ''),  # Image
        (re.compile(r'\[(.*?)\]\(.*?\)'), r'\1'),  # Link
        (re.compile(r'^\s*>+\s*', re.MULTILINE), ''),  # Blockquote
        (
            re.compile(r'(?P<table_block>(?:^[^\n]*\|[^\n]*\n)+)', re.MULTILINE),
            _replace_table_block
        ),
        (re.compile(r'^\s*[*+-]\s*', re.MULTILINE), '- '),  # List
        (re.compile(r'\$\$.*?\$\$', re.DOTALL), ''),  # Block-level formula
        (
            re.compile(r'(?<![A-Za-z0-9])\$([^\n$]+)\$(?![A-Za-z0-9])'),
            _replace_inline_dollar
        ),
        (re.compile(r'\n{2,}'), '\n'),  # Excess blank lines
    ]

    @staticmethod
    def clean_markdown(text: str) -> str:
        """
        Main entry point: apply every regex in order to remove or rewrite Markdown syntax.
        """
        for regex, replacement in MarkdownCleaner.REGEXES:
            text = regex.sub(replacement, text)

        # Strip emoji characters
        text = check_emoji(text)

        # Check whether the text consists entirely of ASCII and basic punctuation
        if text and all((c.isascii() or c.isspace() or c in punctuation_set) for c in text):
            # Preserve original whitespace and return directly
            return text

        return text.strip()

def convert_percentage_to_range(percentage, min_val, max_val, base_val=None):
    """
    Convert a percentage (-100 to 100) into a value within the given range.

    Args:
        percentage: Percentage value (-100 to 100).
        min_val: Minimum value of the target range.
        max_val: Maximum value of the target range.
        base_val: Base value (optional; defaults to the midpoint of the range).

    Returns:
        The converted value.
    """
    percentage, min_val, max_val = float(percentage), float(min_val), float(max_val)
    base_val = float(base_val) if base_val is not None else (min_val + max_val) / 2

    if percentage < 0:
        # Negative percentage: linearly interpolate from base_val toward min_val
        result = base_val + (base_val - min_val) * (percentage / 100)
    else:
        # Positive percentage: linearly interpolate from base_val toward max_val
        result = base_val + (max_val - base_val) * (percentage / 100)

    # Ensure the result stays within the valid range
    return max(min_val, min(max_val, result))
