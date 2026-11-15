"""Common ancestor of generated XML namespace-specific data-object interfaces."""
from ..aii_data_object import AIiDataObject


class AIiXmlDataObjectNamespace(AIiDataObject):
    """Marker for XML namespace identity; JSON and YAML have no such marker."""
