"""Reusable mutable foundation for objects generated from XML Schema definitions."""

from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.xmldefs.aii_xml_smart_data_object import AIiXmlSmartDataObject


class AIcXmlSmartDataObject(AIcSmartDataObject, AIiXmlSmartDataObject):
    """Preserve the XML Schema implementation lineage with the shared SDO runtime.

    Concrete generated subclasses supply schema fields, accessors and metadata.
    No source-technology-specific operations are introduced by this base yet.
    """
