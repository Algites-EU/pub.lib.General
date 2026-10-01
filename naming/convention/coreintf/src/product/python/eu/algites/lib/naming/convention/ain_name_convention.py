from __future__ import annotations

from enum import Enum

class AInNameConvention(str, Enum):
    """Defines the name convention enumeration."""
    AS_IS = "AS_IS"
    LOWER_SNAKE_CASE = "LOWER_SNAKE_CASE"
    UPPER_SNAKE_CASE = "UPPER_SNAKE_CASE"
    LOWER_CAMEL_CASE = "LOWER_CAMEL_CASE"
    UPPER_CAMEL_CASE = "UPPER_CAMEL_CASE"
    LOWER_KEBAB_CASE = "LOWER_KEBAB_CASE"
    UPPER_KEBAB_CASE = "UPPER_KEBAB_CASE"
