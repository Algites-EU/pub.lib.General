"""Mutable SmartDataObject contract for YAML definitions definitions."""

from eu.algites.lib.data.dataobject.yamldefs.aii_yaml_data_object import AIiYamlDataObject
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject


class AIiYamlSmartDataObject(AIiSmartDataObject, AIiYamlDataObject):
    """Combine the shared mutable API with the YAML definitions source marker."""
