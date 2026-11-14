"""Policies for metadata defaults and XSD fixed constraints."""
from enum import Enum

class AInDataObjectFieldDefaultKind(str, Enum):
    """Default behavior; raw presence and serialization stay independent."""
    DECLARATION_ONLY = 'DECLARATION_ONLY'
    IF_FIELD_VALUE_NOT_PRESENT = 'IF_FIELD_VALUE_NOT_PRESENT'
    IF_FIELD_VALUE_EMPTY = 'IF_FIELD_VALUE_EMPTY'
    CONSTRAINT_DEFAULT = 'CONSTRAINT_DEFAULT'
