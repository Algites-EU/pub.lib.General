"""XML Schema declaration kinds."""
from enum import Enum

class AInXmlObjectDefinitionKind(Enum):
    """Separate schema types from globally substitutable XML element identities."""
    TYPE = 'type'
    ELEMENT = 'element'
