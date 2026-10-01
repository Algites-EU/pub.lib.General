from __future__ import annotations

from enum import Enum

class AInInputNameKind(str, Enum):
    """Defines the input name kind enumeration."""
    DEFINITION = "DEFINITION"
    PROPERTY = "PROPERTY"
    ENUM_VALUE = "ENUM_VALUE"
    SYMBOLIC_MAP_KEY = "SYMBOLIC_MAP_KEY"
    PACKAGE_SEGMENT = "PACKAGE_SEGMENT"
    FILE_STEM = "FILE_STEM"
    DATA_TYPE_FILE_STEM = "DATA_TYPE_FILE_STEM"
    ENUM_TYPE_FILE_STEM = "ENUM_TYPE_FILE_STEM"
    INTERFACE_TYPE_FILE_STEM = "INTERFACE_TYPE_FILE_STEM"
