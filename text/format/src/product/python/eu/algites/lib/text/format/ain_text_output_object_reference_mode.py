"""Reference policies for the text rendering of cyclic or shared SDO graphs."""
from enum import Enum


class AInTextOutputObjectReferenceMode(str, Enum):
    """Choose whether a format references object instances or emits values inline.

    These values do not select a schema identity and a text profile may reject
    unsupported policies. Virtual mappings always preserve object identity.
    """
    NO_REFERENCES = 'NO_REFERENCES'
    """Always inline values; shared identity is lost and cycles cannot be encoded."""
    ALL_OBJECTS = 'ALL_OBJECTS'
    """Assign a reference identifier to every eligible object, even unique ones."""
    CYCLIC_REFERENCES_ONLY = 'CYCLIC_REFERENCES_ONLY'
    """Use references to break cycles, but not necessarily to preserve all sharing."""
    REPEATED_OBJECTS = 'REPEATED_OBJECTS'
    """Represent shared objects and cycles as references preserving their identity."""
    OPTIMIZED_TEXT_OUTPUT_SIZE = 'OPTIMIZED_TEXT_OUTPUT_SIZE'
    """Choose references by text-size savings while still representing cycles."""
