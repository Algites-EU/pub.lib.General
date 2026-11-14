"""Format-independent typed SmartDataObject to ordered data-tree conversion."""
from __future__ import annotations
from collections.abc import Mapping
from enum import Enum
from decimal import Decimal
from typing import Any
from .aic_smart_data_object import AIcSmartDataObject

class AIsSmartDataObjectDataTree:
    """Convert SDO values into plain maps/sequences/scalars."""

    @staticmethod
    def toData(aObject: AIcSmartDataObject, aRegistry: object) -> dict[str, Any]:
        """Serialize one root to data-field-name tree with cycle detection."""
        def convert(node: Any, active: set[int]) -> Any:
            if node is None or isinstance(node, (str, int, float, bool, Decimal)):
                return node
            if isinstance(node, Enum):
                if not callable(getattr(node, 'code', None)):
                    raise ValueError('Externally serialized enum must implement code(): ' + repr(type(node)))
                return node.code()
            if id(node) in active: raise ValueError('Cyclic SDO graph requires reference-aware profile')
            active.add(id(node))
            try:
                if isinstance(node, AIcSmartDataObject):
                    binding = aRegistry.getBinding(type(node))
                    if binding is None: raise ValueError('Unregistered SDO implementation')
                    raw = node.getRawValues()
                    return {field.getDataFieldName(): convert(raw[field.getSemanticFieldName()], active)
                            for field in binding.getMetadata().getFields()
                            if field.getSemanticFieldName() in raw}
                if isinstance(node, Mapping):
                    if any(not isinstance(key, str) for key in node):
                        raise ValueError('Data object map keys must be strings')
                    return {key: convert(item, active) for key, item in node.items()}
                if isinstance(node, (list, tuple)):
                    return [convert(item, active) for item in node]
                raise TypeError('Unsupported data tree value: ' + repr(type(node)))
            finally:
                active.remove(id(node))
        return convert(aObject, set())
