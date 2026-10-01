from __future__ import annotations

from enum import Enum

class AInConventionViolationReaction(str, Enum):
    """Defines the convention violation reaction enumeration."""
    IGNORE = "IGNORE"
    INFO = "INFO"
    WARNING = "WARNING"
    ERROR = "ERROR"
