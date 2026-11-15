"""Python equivalent of Java Optional<?> used for field defaults."""
from dataclasses import dataclass
from typing import Any

@dataclass(frozen=True)
class AIcdFieldDefaultValue:
    """A declared default value; None explicitly means null, not missing metadata."""
    value: Any
