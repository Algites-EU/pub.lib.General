from __future__ import annotations

from enum import Enum

class AInVersionSource(str, Enum):
    """Defines the version source enumeration."""
    EXPLICIT_METADATA = "EXPLICIT_METADATA"
    FILE_NAME_SUFFIX = "FILE_NAME_SUFFIX"
    EXPLICIT_THEN_FILE_NAME = "EXPLICIT_THEN_FILE_NAME"
