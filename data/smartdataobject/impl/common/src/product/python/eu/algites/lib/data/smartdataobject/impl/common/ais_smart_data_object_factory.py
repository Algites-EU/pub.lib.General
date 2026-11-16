"""Format-neutral SDO factory, selecting implementations only from binding registry."""
from __future__ import annotations
from collections.abc import Mapping
from decimal import Decimal
from enum import Enum
from importlib import import_module
import re
from typing import Any, get_args, get_origin, get_type_hints

from .aic_smart_data_object import AIcSmartDataObject
from .aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry

def resolve_effective_return_type(field: Any, registry: AIcSmartDataObjectBindingRegistry) -> Any:
    """Resolve generated forward annotations against registered SDO contracts.

    Codegen keeps nested contracts behind TYPE_CHECKING to avoid circular
    imports. The owning purpose-scoped registry supplies the concrete types
    when unmarshalling; never guess a type from an unqualified field name.
    """
    raw = field.getEffectiveReturnType()
    if not isinstance(raw, str):
        return raw
    from eu.algites.lib.common.list.aii_bounded_list import AIiBoundedList
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    known: dict[str, type] = {
        'AIiBoundedList': AIiBoundedList,
        'AIiMultiMappedList': AIiMultiMappedList,
    }
    ambiguous: set[str] = set()
    # The Defs emitter declares enum imports explicitly on read contracts.
    # Setter-oriented generated implementations omit these imports to avoid
    # cycles, and may override the original typed getter without an annotation.
    # Resolve only named, declared scalar types; never infer an enum from data.
    referenced = frozenset(re.findall(r'\b[A-Za-z_]\w*\b', raw))
    for binding in registry.getBindings():
        for candidate in binding.getImplementationClass().__mro__:
            if candidate is object:
                continue
            previous = known.get(candidate.__name__)
            if previous is not None and previous is not candidate:
                ambiguous.add(candidate.__name__)
            else:
                known[candidate.__name__] = candidate
            declarations = candidate.__dict__.get('__xml_scalar_type_imports__', {})
            if not isinstance(declarations, dict):
                raise TypeError('Invalid generated XML scalar type declarations')
            for qualified in declarations.values():
                module_name, separator, class_name = qualified.partition(':')
                if class_name not in referenced:
                    continue
                if not separator or not module_name or not class_name:
                    raise ValueError('Invalid generated scalar import: ' + repr(qualified))
                enum_type = getattr(import_module(module_name), class_name)
                if not isinstance(enum_type, type) or not issubclass(enum_type, Enum):
                    raise TypeError('Generated scalar type is not an enum: ' + qualified)
                previous = known.get(class_name)
                if previous is not None and previous is not enum_type:
                    ambiguous.add(class_name)
                else:
                    known[class_name] = enum_type
    if referenced.intersection(ambiguous):
        raise ValueError('Ambiguous generated forward annotation: ' + raw)
    getter = field.getEffectiveGetter()
    try:
        resolved = get_type_hints(getter, localns=known).get('return')
    except (NameError, TypeError, AttributeError) as exc:
        raise ValueError('Cannot resolve generated field return type ' + raw +
                         '; register its referenced SDO implementation') from exc
    if resolved is None:
        # Inherited metadata retains the typed read-only getter when an SDO
        # override intentionally has no return annotation of its own.
        resolved = known.get(raw)
    if resolved is None:
        raise ValueError('Missing generated field return type: ' + raw)
    return resolved


class AIsSmartDataObjectFactory:
    """Create and populate a registered SDO from decoded canonical data fields."""

    @staticmethod
    def fromData(aReadContract: type, aProperties: Mapping[str, Any],
                 aRegistry: AIcSmartDataObjectBindingRegistry) -> AIcSmartDataObject:
        """Select a concrete implementation and materialize one SDO graph."""
        if not isinstance(aProperties, Mapping):
            raise ValueError('SDO root must be a mapping')
        binding = aRegistry.resolveBinding(aReadContract)
        return _materialize(binding, aProperties, aRegistry, {})

    @staticmethod
    def fromImplementationData(aImplementationClass: type, aProperties: Mapping[str, Any],
                               aRegistry: AIcSmartDataObjectBindingRegistry) -> AIcSmartDataObject:
        """Populate an exactly selected registered SDO implementation."""
        binding = aRegistry.getBinding(aImplementationClass)
        if binding is None:
            raise ValueError('Implementation not registered: ' + repr(aImplementationClass))
        return _materialize(binding, aProperties, aRegistry, {})


def _materialize(binding, data: Mapping[str, Any], registry, visited):
    if id(data) in visited: return visited[id(data)]
    try: instance = binding.getImplementationClass()()
    except TypeError as exc:
        raise ValueError('A zero-argument SDO constructor is required') from exc
    visited[id(data)] = instance
    by_name = {}
    for field in binding.getMetadata().getFields():
        by_name[field.getDataFieldName()] = field
        by_name.setdefault(field.getSemanticFieldName(), field)
    assigned_semantics: set[str] = set()
    for name, raw_value in data.items():
        field = by_name.get(name)
        if field is None: raise ValueError('Unknown SDO data field: ' + str(name))
        semantic = field.getSemanticFieldName()
        if semantic in assigned_semantics:
            raise ValueError('Duplicate aliases for semantic SDO field: ' + semantic)
        assigned_semantics.add(semantic)
        requested = resolve_effective_return_type(field, registry)
        # Generated collection getters are getter-only. Build the bounded (or
        # indexed) list through the owning SDO, never replace it with a plain
        # list or look for a non-existent collection setter.
        from eu.algites.lib.common.list.aii_bounded_list import AIiBoundedList
        from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
        collection_type = get_origin(requested) or requested
        if (isinstance(raw_value, (list, tuple)) and
                isinstance(collection_type, type) and
                issubclass(collection_type, AIiBoundedList)):
            mapped = issubclass(collection_type, AIiMultiMappedList)
            items = instance.get_OrCreateCollectionField(semantic, mapped)
            item_types = get_args(requested)
            for item in raw_value:
                items.append(_convert(item, item_types[0] if item_types else Any,
                                      binding, semantic, registry, visited))
        else:
            converted = _convert(raw_value, requested, binding, semantic,
                                 registry, visited)
            instance.set_RawField(semantic, converted)
    return instance


def _convert(value: Any, requested: Any, owner, field: str, registry, visited):
    if value is None: return None
    origin = get_origin(requested)
    args = get_args(requested)
    if origin in (list, tuple) and isinstance(value, list):
        if id(value) in visited: return visited[id(value)]
        result = []
        visited[id(value)] = result
        result.extend(_convert(item, args[0] if args else Any, owner, field, registry, visited)
                      for item in value)
        return result
    if origin in (dict, Mapping) and isinstance(value, Mapping):
        if id(value) in visited: return visited[id(value)]
        result = {}
        visited[id(value)] = result
        for key, item in value.items():
            result[key] = _convert(item, args[1] if len(args) > 1 else Any,
                                   owner, field, registry, visited)
        return result
    if isinstance(requested, type):
        from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
        if issubclass(requested, AIiDataObject) and isinstance(value, Mapping):
            return _materialize(registry.resolveBinding(requested, owner, field), value, registry, visited)
        if issubclass(requested, Enum) and isinstance(value, str):
            if any(not callable(getattr(member, 'code', None)) for member in requested):
                raise ValueError('Externally serialized enum must implement code(): ' + repr(requested))
            for member in requested:
                if member.code() == value: return member
            raise ValueError('Unknown enumeration code: ' + value)
        if requested is bool:
            if type(value) is bool: return value
            if isinstance(value, str):
                if value in ('true', '1'): return True
                if value in ('false', '0'): return False
            raise ValueError('Invalid boolean for ' + field)
        if requested is int:
            if isinstance(value, bool): raise ValueError('Boolean cannot become integer: ' + field)
            if isinstance(value, (str, int, float, Decimal)):
                decimal = Decimal(str(value))
                if not decimal.is_finite() or decimal != decimal.to_integral_value():
                    raise ValueError('Fractional or non-finite value cannot become int: ' + field)
                return int(decimal)
            raise ValueError('Invalid integer for ' + field)
        if requested is float:
            if isinstance(value, bool): raise ValueError('Boolean cannot become float: ' + field)
            if isinstance(value, (str, int, float, Decimal)):
                converted = float(value)
                if not __import__('math').isfinite(converted):
                    raise ValueError('Non-finite float for ' + field)
                return converted
            raise ValueError('Invalid float for ' + field)
        if requested is Decimal:
            if isinstance(value, bool): raise ValueError('Boolean cannot become Decimal: ' + field)
            if isinstance(value, (str, int, float, Decimal)):
                converted = Decimal(str(value))
                if not converted.is_finite():
                    raise ValueError('Non-finite Decimal for ' + field)
                return converted
            raise ValueError('Invalid Decimal for ' + field)
        if isinstance(value, requested): return value
        raise ValueError('Value of type ' + type(value).__name__ + ' cannot become ' +
                         requested.__name__ + ' for field ' + field)
    return value
