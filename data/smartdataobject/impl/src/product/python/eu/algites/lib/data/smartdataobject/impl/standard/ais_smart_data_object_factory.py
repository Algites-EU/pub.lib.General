"""Shared factory for reconstructing graph-typed SmartDataObjects."""
from __future__ import annotations

from collections.abc import Mapping
from contextvars import ContextVar
from typing import Any
from eu.algites.lib.data.smartdataobject.aiid_virtual_field_value import AIidVirtualFieldValue


class _AIcMappingContext:
    """Scope instances by source-container identity, including recursive collections."""
    def __init__(self):
        self.items: dict[int, tuple[object, object]] = {}

    def find(self, source: object) -> object | None:
        item = self.items.get(id(source))
        if item is not None:
            if item[0] is not source:
                raise RuntimeError('Identity collision in typed SDO mapping')
            return item[1]
        return None

    def register(self, source: object, target: object):
        prior = self.find(source)
        if prior is not None and prior is not target:
            raise ValueError('One mapping cannot represent two SmartDataObjects')
        self.items[id(source)] = (source, target)


_CURRENT: ContextVar[_AIcMappingContext | None] = ContextVar('algites_sdo_mapping', default=None)


class AIsSmartDataObjectFactory:
    """Construct SDO graphs using concrete classes supplied by virtual values.

    The public entry point creates a fresh context; nested generated constructors
    reuse it. ContextVar tokens make cleanup safe even when construction fails.
    """
    @staticmethod
    def from_mapping(a_class: type, a_mapping: Mapping[str, AIidVirtualFieldValue]):
        from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
        if not isinstance(a_class, type) or not issubclass(a_class, AIcSmartDataObject):
            raise TypeError('A concrete AIcSmartDataObject class is required')
        if not isinstance(a_mapping, Mapping):
            raise TypeError('Expected a typed field mapping')
        active = _CURRENT.get()
        token = None
        if active is None:
            active = _AIcMappingContext()
            token = _CURRENT.set(active)
        try:
            existing = active.find(a_mapping)
            if existing is not None:
                if not isinstance(existing, a_class):
                    raise TypeError('Shared SDO mapping used with incompatible concrete types')
                return existing
            return a_class(a_mapping)
        finally:
            if token is not None:
                _CURRENT.reset(token)

    @staticmethod
    def populate(a_object, a_mapping: Mapping[str, AIidVirtualFieldValue]):
        active = _CURRENT.get()
        token = None
        if active is None:
            active = _AIcMappingContext()
            token = _CURRENT.set(active)
        try:
            active.register(a_mapping, a_object)
            definitions = a_object._fields()
            for name, wrapped in a_mapping.items():
                if name not in definitions:
                    raise ValueError('Unknown DataObject field: ' + name)
                a_object.set_RawField(name, AIsSmartDataObjectFactory._convert(wrapped, active))
        finally:
            if token is not None:
                _CURRENT.reset(token)

    @staticmethod
    def _convert(wrapped: AIidVirtualFieldValue, context: _AIcMappingContext):
        from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
        if not isinstance(wrapped, AIidVirtualFieldValue):
            raise TypeError('Every virtual field requires AIidVirtualFieldValue')
        source = wrapped.getValueRepresentation()
        if source is None:
            return None
        existing = context.find(source)
        if existing is not None:
            return existing
        concrete = wrapped.getValueClass()
        if isinstance(concrete, type) and issubclass(concrete, AIcSmartDataObject):
            if not isinstance(source, Mapping):
                raise ValueError('SmartDataObject representation must be a field mapping')
            return AIsSmartDataObjectFactory.from_mapping(concrete, source)
        if isinstance(source, list):
            result: list[Any] = []
            context.register(source, result)
            result.extend(AIsSmartDataObjectFactory._convert(value, context) for value in source)
            return result
        if isinstance(source, Mapping):
            result: dict[Any, Any] = {}
            context.register(source, result)
            for key, value in source.items():
                result[AIsSmartDataObjectFactory._convert(key, context)] = AIsSmartDataObjectFactory._convert(value, context)
            return result
        return source
