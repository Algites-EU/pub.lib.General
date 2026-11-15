"""XML Schema identity of an XML DataObject contract."""
from dataclasses import dataclass
from .ain_xml_object_definition_kind import AInXmlObjectDefinitionKind


@dataclass(frozen=True, slots=True)
class AIaDataObjectXmlSchema:
    """XML declaration kind and QName for the format-specific DataObject."""
    definition_kind: AInXmlObjectDefinitionKind = AInXmlObjectDefinitionKind.TYPE
    namespace: str = ''
    namespace_local_name: str = ''
