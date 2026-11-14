from enum import Enum

class AInTextFormatType(str, Enum):
    """Built-in text syntax categories."""
    JSON = 'JSON'
    YAML = 'YAML'
    XML = 'XML'

    def code(self) -> str:
        """Return the stable external format code."""
        return self.value
