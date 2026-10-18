"""Read-only marker for contracts originating from YAML definitions."""

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject


class AIiYamlDataObject(AIiDataObject):
    """Identify YAML definitions contracts without requiring mutable or wire-specific behavior."""
