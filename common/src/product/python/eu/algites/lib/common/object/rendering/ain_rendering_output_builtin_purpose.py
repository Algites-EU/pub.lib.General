from enum import Enum
class AInRenderingOutputBuiltinPurpose(str, Enum):
    DEFAULT = 'DEFAULT'
    USER = 'USER'
    SYSTEM = 'SYSTEM'
    DEBUG = 'DEBUG'
    def code(self) -> str: return self.value
