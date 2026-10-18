"""Reusable mutable foundation for objects generated from JSON Schema definitions."""

from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.jsondefs.aii_json_smart_data_object import AIiJsonSmartDataObject


class AIcJsonSmartDataObject(AIcSmartDataObject, AIiJsonSmartDataObject):
    """Preserve the JSON Schema implementation lineage with the shared SDO runtime.

    Concrete generated subclasses supply schema fields, accessors and metadata.
    No source-technology-specific operations are introduced by this base yet.
    """
