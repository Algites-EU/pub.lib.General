"""Independent lazy metadata registry based on actual Python type identity."""
from __future__ import annotations
from dataclasses import dataclass, field
from threading import RLock
from typing import Any, get_type_hints
from .ais_smart_data_object_format_binding_provider_registry import AIsSmartDataObjectFormatBindingProviderRegistry
from .aic_smart_data_object import AIcSmartDataObject

@dataclass(frozen=True)
class _Field:
    semantic: str
    data: str
    getter: Any
    definition: Any
    return_type: Any
    string_constraints: Any = None
    numeric_constraints: Any = None
    list_constraints: Any = None
    object_constraints: Any = None
    key_definitions: tuple[Any, ...] = ()
    def getStringConstraints(self): return self.string_constraints
    def getNumericConstraints(self): return self.numeric_constraints
    def getListConstraints(self): return self.list_constraints
    def getObjectConstraints(self): return self.object_constraints
    def getKeyDefinitions(self) -> tuple[Any, ...]: return self.key_definitions
    def getSemanticFieldName(self) -> str: return self.semantic
    def getDataFieldName(self) -> str: return self.data
    def getEffectiveGetter(self): return self.getter
    def getEffectiveReturnType(self): return self.return_type
    def getDefinition(self): return self.definition

@dataclass
class _Metadata:
    implementation: type
    fields: dict[str, _Field]
    format_bindings: dict[type, Any] = field(default_factory=dict)
    lock: Any = field(default_factory=RLock)
    _initialized: bool = False
    def getImplementationClass(self) -> type: return self.implementation
    def getFields(self) -> tuple[_Field, ...]: return tuple(self.fields.values())
    def getField(self, aSemanticFieldName: str) -> _Field | None:
        return self.fields.get(aSemanticFieldName)
    def getFormatBindings(self) -> tuple[Any, ...]:
        with self.lock: return tuple(x for x in self.format_bindings.values() if x is not None)
    def getFormatBinding(self, aFormatId: str, aProviderId: str) -> Any | None:
        hits = [x for x in self.getFormatBindings()
                if x.getFormatId() == aFormatId and x.getProviderId() == aProviderId]
        if len(hits) > 1:
            raise ValueError('Ambiguous format binding provider: ' + aFormatId + '/' + aProviderId)
        return hits[0] if hits else None
    def ensureInitialized(self) -> None:
        """Populate format metadata once from the process-cached providers.

        Common fields are scanned first, so providers can inspect the complete
        common descriptor. An exception leaves initialization retryable.
        """
        if self._initialized:
            return
        with self.lock:
            if self._initialized:
                return
            for provider in AIsSmartDataObjectFormatBindingProviderRegistry.getProviders():
                if type(provider) not in self.format_bindings:
                    result = provider.resolveMetadata(self)
                    if result is not None and (result.getFormatId() != provider.getFormatId() or
                                               result.getProviderId() != provider.getProviderId()):
                        raise ValueError('Provider metadata identity mismatch')
                    self.format_bindings[type(provider)] = result
            self._initialized = True

class AIsSmartDataObjectMetadataRegistry:
    """One lazy metadata object per type, independent of the legacy metadata cache."""
    _lock = RLock()

    @classmethod
    def forType(cls, aImplementationClass: type) -> _Metadata:
        """Return cached common and eagerly resolved format-specific metadata."""
        if not isinstance(aImplementationClass, type) or not issubclass(aImplementationClass, AIcSmartDataObject):
            raise TypeError('Expected an AIcSmartDataObject implementation class')
        with cls._lock:
            result = aImplementationClass.__dict__.get('__sdo_new_runtime_metadata_cache__')
            if result is None:
                result = _Metadata(aImplementationClass, cls._scan(aImplementationClass))
                # Class-owned cache does not pin dynamic classes in a global mapping.
                setattr(aImplementationClass, '__sdo_new_runtime_metadata_cache__', result)
        # Format metadata are populated immediately for a new implementation.
        # On subsequent field accesses this is just a boolean check, not discovery.
        result.ensureInitialized()
        return result

    @staticmethod
    def _scan(implementation: type) -> dict[str, _Field]:
        found: dict[str, _Field] = {}
        for owner in reversed(implementation.__mro__):
            for name, getter in owner.__dict__.items():
                if not name.startswith('get') or len(name) < 4 or not name[3].isupper() or not callable(getter):
                    continue
                semantic = name[3:]
                # The new metadata interpreter owns its own cache and deliberately does
                # not consult the legacy __sdo_runtime_effective_fields_cache__.
                definition = getattr(getter, '__sdo_new_field_definition__', None)
                earlier = found.get(semantic)
                if definition is None and earlier is None:
                    continue
                effective_definition = definition or earlier.definition
                new_data_name = getattr(definition, 'dataFieldName', '') if definition else ''
                data_name = (new_data_name or (earlier.data if earlier is not None else semantic))
                if earlier is not None and new_data_name and new_data_name != earlier.data:
                    raise ValueError('Cannot change inherited dataFieldName: ' + semantic)
                try:
                    return_type = get_type_hints(getter).get('return')
                except (NameError, TypeError):
                    return_type = getattr(getter, '__annotations__', {}).get('return')
                if return_type is None and earlier is not None:
                    return_type = earlier.return_type
                def inherited_constraint(kind: str, previous: Any) -> Any:
                    """Use the nearest explicitly annotated constraint, including overrides."""
                    explicit = getter.__dict__.get('__data_object_field_' + kind + '_constraints__')
                    return explicit if explicit is not None else previous
                owner_keys = owner.__dict__.get('__key_definitions__', {})
                keys = getter.__dict__.get('__key_definitions__')
                if keys is None and isinstance(owner_keys, dict):
                    keys = owner_keys.get(semantic, owner_keys.get(data_name))
                found[semantic] = _Field(
                    semantic, data_name, getter, effective_definition, return_type,
                    inherited_constraint('string', earlier.string_constraints if earlier else None),
                    inherited_constraint('numeric', earlier.numeric_constraints if earlier else None),
                    inherited_constraint('list', earlier.list_constraints if earlier else None),
                    inherited_constraint('object', earlier.object_constraints if earlier else None),
                    tuple(keys) if keys is not None else (earlier.key_definitions if earlier else ()))
        # Empty schema types are valid and have a zero-field descriptor.
        names = [fld.data for fld in found.values() if not getattr(fld.definition, 'embedded', False)]
        if len(names) != len(set(names)):
            raise ValueError('Duplicate dataFieldName on ' + implementation.__qualname__)
        return found
