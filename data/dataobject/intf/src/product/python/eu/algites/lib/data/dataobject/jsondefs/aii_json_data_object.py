"""Read-only marker for contracts originating from JSON Schema."""

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject


class AIiJsonDataObject(AIiDataObject):
    """Identify JSON Schema contracts without requiring mutable or wire-specific behavior."""
