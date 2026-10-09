from __future__ import annotations

from enum import Enum

class AInOutputNameKind(str, Enum):
    """Defines the output name kind enumeration."""
    DATA_TYPE = "DATA_TYPE"
    ENUM_TYPE = "ENUM_TYPE"
    INTERFACE_TYPE = "INTERFACE_TYPE"
    PROPERTY = "PROPERTY"
    ENUM_CONSTANT = "ENUM_CONSTANT"
    PACKAGE_SEGMENT = "PACKAGE_SEGMENT"
    FILE_STEM = "FILE_STEM"
    DATA_TYPE_FILE_STEM = "DATA_TYPE_FILE_STEM"
    ENUM_TYPE_FILE_STEM = "ENUM_TYPE_FILE_STEM"
    INTERFACE_TYPE_FILE_STEM = "INTERFACE_TYPE_FILE_STEM"
