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
