"""XML Schema wire metadata for a DataObject getter."""
from dataclasses import dataclass
from .ain_xml_value_kind import AInXmlValueKind


@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldXmlSchema:
    """XML QName and ELEMENT/ATTRIBUTE kind; shared field order is elsewhere."""
    namespace: str = ''
    namespace_local_name: str = ''
    value_kind: AInXmlValueKind = AInXmlValueKind.ELEMENT
