from __future__ import annotations

from enum import Enum

class AInConventionSubject(str, Enum):
    """Defines the convention subject enumeration."""
    DEFINITION_NAME = "DEFINITION_NAME"
    PROPERTY_NAME = "PROPERTY_NAME"
    ENUM_VALUE = "ENUM_VALUE"
    SYMBOLIC_MAP_KEY = "SYMBOLIC_MAP_KEY"
    PACKAGE_SEGMENT = "PACKAGE_SEGMENT"
    INPUT_VERSION = "INPUT_VERSION"
    OUTPUT_VERSION = "OUTPUT_VERSION"
