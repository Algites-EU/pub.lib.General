from enum import Enum
class AInRenderingOutputBuiltinFormat(str, Enum):
    DEFAULT = 'DEFAULT'
    PLAIN_TEXT = 'PLAIN_TEXT'
    XML = 'XML'
    JSON = 'JSON'
    def code(self) -> str: return self.value
