"""XML QName and element/attribute metadata for a common data-object field."""
from dataclasses import dataclass
from .ain_xml_value_kind import AInXmlValueKind

@dataclass(frozen=True, slots=True)
class AIaXmlDataObjectField:
    """XML-specific QName and wire representation; never owns a field identifier.

    A Python contract associates a field name with this metadata by using the
    ``__xml_data_object_fields__`` mapping on the contract class. The annotation
    itself describes only the XML wire representation, as in the Java API.
    """
    namespace: str = ''
    namespace_local_name: str = ''
    value_kind: AInXmlValueKind = AInXmlValueKind.ELEMENT
    order: int = -1
    min_occurs: int = -1
    max_occurs: int = -1
    wildcard_namespace: str = ''
    wildcard_process_contents: str = 'strict'

    def __post_init__(self):
        """Reject negative finite maxima and inverted XML occurrence ranges."""
        if self.wildcard_namespace:
            if self.value_kind is not AInXmlValueKind.ELEMENT:
                raise ValueError('xs:any must be represented as an XML element')
            if self.wildcard_process_contents not in ('skip', 'lax', 'strict'):
                raise ValueError('Invalid XML wildcard processContents')
        if self.min_occurs < -1 or self.max_occurs < -1:
            raise ValueError('Invalid XML occurrence bound')
        if self.min_occurs >= 0 and self.max_occurs >= 0 and self.min_occurs > self.max_occurs:
            raise ValueError('XML min_occurs exceeds max_occurs')
