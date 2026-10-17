"""Simple format-specific scalar escaping used by generic text output tools."""
import json
from xml.sax.saxutils import escape

class AIsTextFormatEscapes:
    """Static-only encoder catalog."""
    def __new__(cls):
        raise TypeError('Static utility class')
    @staticmethod
    def json_string(value: str) -> str:
        return json.dumps(value, ensure_ascii=False)
    @staticmethod
    def xml_text(value: str) -> str:
        return escape(value, {'"': '&quot;', "'": '&apos;'})
