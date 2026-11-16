"""Purpose-scoped SDO implementation registry with inherited binding metadata."""
from __future__ import annotations
from dataclasses import dataclass
from typing import Any, get_args, get_origin
from .ais_smart_data_object_metadata_registry import AIsSmartDataObjectMetadataRegistry
from .aic_smart_data_object import AIcSmartDataObject

@dataclass(frozen=True)
class _FieldBinding:
    field: Any
    selected: type | None
    def getFieldMetadata(self) -> Any: return self.field
    def getImplementationClassOverride(self) -> type | None: return self.selected

@dataclass(frozen=True)
class _Binding:
    implementation: type
    metadata: Any
    declared: frozenset[type]
    effective: frozenset[type]
    field_bindings: dict[str, _FieldBinding]
    def getImplementationClass(self) -> type: return self.implementation
    def getMetadata(self) -> Any: return self.metadata
    def getDeclaredReadContracts(self) -> frozenset[type]: return self.declared
    def getEffectiveReadContracts(self) -> frozenset[type]: return self.effective
    def getFieldBindings(self) -> tuple[_FieldBinding, ...]: return tuple(self.field_bindings.values())
    def getFieldBinding(self, aSemanticFieldName: str) -> _FieldBinding | None:
        return self.field_bindings.get(aSemanticFieldName)

class AIcSmartDataObjectBindingRegistry:
    """Immutable selected implementation bindings; never a global implementation catalogue."""
    def __init__(self, bindings: dict[type, _Binding]):
        self._bindings = dict(bindings)
        index: dict[type, list[_Binding]] = {}
        for binding in self._bindings.values():
            for contract in binding.effective:
                index.setdefault(contract, []).append(binding)
        self._index = {k: tuple(v) for k, v in index.items()}

    @classmethod
    def builder(cls):
        """Create a builder for a purpose-scoped immutable registry."""
        return _Builder()

    def getBindings(self) -> tuple[_Binding, ...]:
        return tuple(self._bindings.values())

    def getBinding(self, aImplementationClass: type) -> _Binding | None:
        return self._bindings.get(aImplementationClass)

    def getBindingsForReadContract(self, aReadContract: type) -> tuple[_Binding, ...]:
        return self._index.get(aReadContract, ())

    def resolveBinding(self, aReadContract: type, aOwningBinding: _Binding | None = None,
                       aSemanticFieldName: str | None = None) -> _Binding:
        """Resolve one implementation; conflicts are errors at use, not registration."""
        if aOwningBinding is not None and aSemanticFieldName is not None:
            field = aOwningBinding.getFieldBinding(aSemanticFieldName)
            explicit = field.getImplementationClassOverride() if field else None
            if explicit is not None:
                selected = self.getBinding(explicit)
                if selected is None:
                    raise ValueError('Explicit field implementation is not registered: ' + repr(explicit))
                if not issubclass(explicit, aReadContract):
                    raise ValueError('Explicit field implementation violates read contract')
                return selected
        candidates = self.getBindingsForReadContract(aReadContract)
        if not candidates:
            # Mutable generated contracts extend their read-only equivalents.
            # A nested getter may explicitly return the mutable contract,
            # which is still satisfied by its registered concrete SDO class.
            candidates = tuple(binding for binding in self._bindings.values()
                               if issubclass(binding.implementation, aReadContract))
        primary = [binding for binding in candidates if aReadContract in binding.declared]
        if len(primary) == 1: return primary[0]
        if not primary and len(candidates) == 1: return candidates[0]
        if not candidates: raise ValueError('No registered implementation for ' + repr(aReadContract))
        raise ValueError('Ambiguous SDO implementations: ' + ', '.join(
            item.implementation.__qualname__ for item in candidates))

class _Builder:
    """Accumulate types, retaining multiple implementations of one contract."""
    def __init__(self): self._bindings: dict[type, _Binding] = {}
    def register(self, aImplementationClass: type):
        """Register exactly one concrete implementation, inspecting all its ancestors."""
        if not isinstance(aImplementationClass, type) or not issubclass(aImplementationClass, AIcSmartDataObject):
            raise TypeError('Implementation must derive from AIcSmartDataObject')
        if aImplementationClass in self._bindings:
            return self
        metadata = AIsSmartDataObjectMetadataRegistry.forType(aImplementationClass)
        declared: set[type] = set()
        effective: set[type] = set()
        for ancestor in reversed(aImplementationClass.__mro__):
            annotation = ancestor.__dict__.get('__sdo_implementation_binding__')
            if annotation is None: continue
            for contract in annotation.readContracts:
                if not issubclass(aImplementationClass, contract):
                    raise ValueError(f'{aImplementationClass.__qualname__} does not implement {contract}')
                if ancestor is aImplementationClass: declared.add(contract)
                effective.update(c for c in contract.__mro__ if c is not object)
        if not effective:
            raise ValueError('Missing inherited implementation binding: ' + aImplementationClass.__qualname__)
        overrides: dict[str, type] = {}
        for ancestor in reversed(aImplementationClass.__mro__):
            for name, getter in ancestor.__dict__.items():
                if name.startswith('get') and callable(getter):
                    annotation = getattr(getter, '__sdo_field_implementation_binding__', None)
                    if annotation is not None: overrides[name[3:]] = annotation.implementationClass
        fields: dict[str, _FieldBinding] = {}
        for field in metadata.getFields():
            name = field.getSemanticFieldName()
            raw_type = field.getEffectiveReturnType()
            selected = overrides.get(name)
            if selected is None and not _contains_sdo(raw_type): continue
            if selected is not None and not _type_accepts(raw_type, selected):
                raise ValueError('Incompatible field binding for ' + name)
            fields[name] = _FieldBinding(field, selected)
        self._bindings[aImplementationClass] = _Binding(
            aImplementationClass, metadata, frozenset(declared), frozenset(effective), fields)
        return self
    def build(self) -> AIcSmartDataObjectBindingRegistry:
        """Freeze the bindings and reverse contract lookup."""
        return AIcSmartDataObjectBindingRegistry(self._bindings)

def _contains_sdo(return_type: Any) -> bool:
    origin = get_origin(return_type)
    if origin is not None:
        return any(_contains_sdo(t) for t in get_args(return_type))
    return isinstance(return_type, type) and any(c.__name__ == 'AIiDataObject' for c in return_type.__mro__)

def _type_accepts(return_type: Any, implementation: type) -> bool:
    origin = get_origin(return_type)
    if origin is not None:
        return any(_type_accepts(t, implementation) for t in get_args(return_type))
    return isinstance(return_type, type) and issubclass(implementation, return_type)
