"""XML QName identity of a type or global element contract."""
from dataclasses import dataclass
from .ain_xml_object_definition_kind import AInXmlObjectDefinitionKind

@dataclass(frozen=True, slots=True)
class AIaXmlDataObject:
    """XML declaration QName independent of common schema ID and Python type name."""
    __deprecated__ = 'Deprecated since 1.1: use AIaDataObjectXmlSchema (legacy compatibility retained)'
    definition_kind: AInXmlObjectDefinitionKind = AInXmlObjectDefinitionKind.TYPE
    namespace: str = ''
    namespace_local_name: str = ''
