"""XML wire representations for schema field getters."""
from enum import Enum

class AInXmlValueKind(Enum):
    """A field is a child element by default or a scalar attribute when selected."""
    ELEMENT = 'element'
    ATTRIBUTE = 'attribute'
