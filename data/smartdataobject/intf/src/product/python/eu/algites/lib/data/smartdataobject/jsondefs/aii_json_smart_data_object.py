"""Mutable SmartDataObject contract for JSON Schema definitions."""

from eu.algites.lib.data.dataobject.jsondefs.aii_json_data_object import AIiJsonDataObject
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject


class AIiJsonSmartDataObject(AIiSmartDataObject, AIiJsonDataObject):
    """Combine the shared mutable API with the JSON Schema source marker."""
