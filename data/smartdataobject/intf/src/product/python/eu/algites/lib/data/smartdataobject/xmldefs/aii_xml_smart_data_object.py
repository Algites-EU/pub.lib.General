"""Mutable SmartDataObject contract for XML Schema definitions."""

from eu.algites.lib.data.dataobject.xmldefs.aii_xml_data_object import AIiXmlDataObject
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject


class AIiXmlSmartDataObject(AIiSmartDataObject, AIiXmlDataObject):
    """Combine the shared mutable API with the XML Schema source marker."""
