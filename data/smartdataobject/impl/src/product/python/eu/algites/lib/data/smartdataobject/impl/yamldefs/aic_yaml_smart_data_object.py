"""Reusable mutable foundation for objects generated from YAML definitions definitions."""

from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.yamldefs.aii_yaml_smart_data_object import AIiYamlSmartDataObject


class AIcYamlSmartDataObject(AIcSmartDataObject, AIiYamlSmartDataObject):
    """Preserve the YAML definitions implementation lineage with the shared SDO runtime.

    Concrete generated subclasses supply schema fields, accessors and metadata.
    No source-technology-specific operations are introduced by this base yet.
    """
