"""Common ancestor of generated XML namespace-specific data-object interfaces."""
from .aii_xml_data_object import AIiXmlDataObject


class AIiXmlDataObjectNamespace(AIiXmlDataObject):
    """Marker for XML namespace identity; JSON and YAML have no such marker."""
