from enum import Enum

class AInTextOutputObjectReferenceMode(str, Enum):
    """Identity-preserving text output policies."""
    NO_REFERENCES = 'NO_REFERENCES'
    ALL_OBJECTS = 'ALL_OBJECTS'
    CYCLIC_REFERENCES_ONLY = 'CYCLIC_REFERENCES_ONLY'
    REPEATED_OBJECTS = 'REPEATED_OBJECTS'
    OPTIMIZED_TEXT_OUTPUT_SIZE = 'OPTIMIZED_TEXT_OUTPUT_SIZE'
