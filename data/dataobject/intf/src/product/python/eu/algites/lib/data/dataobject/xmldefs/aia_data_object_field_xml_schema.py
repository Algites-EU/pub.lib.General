"""XML Schema wire metadata of a format-specific DataObject getter."""
from dataclasses import dataclass
from .ain_xml_value_kind import AInXmlValueKind


@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldXmlSchema:
    """XML QName, XML value kind, and compatibility wildcard metadata."""
    namespace: str = ''
    namespace_local_name: str = ''
    value_kind: AInXmlValueKind = AInXmlValueKind.ELEMENT
    order: int = -1
    wildcard_namespace: str = ''
    wildcard_process_contents: str = 'strict'

    def __post_init__(self):
        """Validate wildcard options without interpreting schema-neutral cardinality."""
        if self.wildcard_namespace:
            if self.value_kind is not AInXmlValueKind.ELEMENT:
                raise ValueError('xs:any must be represented as an XML element')
            if self.wildcard_process_contents not in ('skip', 'lax', 'strict'):
                raise ValueError('Invalid XML wildcard processContents')
