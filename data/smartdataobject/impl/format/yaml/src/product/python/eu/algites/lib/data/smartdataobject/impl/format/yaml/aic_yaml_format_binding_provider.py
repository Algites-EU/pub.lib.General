"""YAML format provider with safe loading and no arbitrary object constructors."""
from __future__ import annotations
from dataclasses import dataclass
from typing import Any
import yaml
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_data_tree import AIsSmartDataObjectDataTree
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_factory import AIsSmartDataObjectFactory

@dataclass(frozen=True)
class _YamlMetadata:
    """YAML binding metadata derived from JSON Schema metadata."""
    definition: Any
    def getFormatId(self) -> str: return 'YAML'
    def getProviderId(self) -> str: return 'algites-yaml'

class _NoDuplicateSafeLoader(yaml.SafeLoader):
    """Safe YAML loader that rejects ambiguous duplicate mapping keys."""

def _unique_map(loader, node):
    result = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=True)
        if not isinstance(key, (str, int, float, bool, type(None))):
            raise ValueError('Unsupported YAML key type')
        if key in result: raise ValueError('Duplicate YAML key: ' + str(key))
        result[key] = loader.construct_object(value_node, deep=True)
    return result
_NoDuplicateSafeLoader.add_constructor(yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, _unique_map)

class _Marshaller:
    def marshal(self, aObject, aRegistry) -> str:
        return yaml.safe_dump(AIsSmartDataObjectDataTree.toData(aObject, aRegistry),
                              allow_unicode=True, sort_keys=False)

class _Unmarshaller:
    def unmarshal(self, aText: str, aReadContract: type, aRegistry):
        if len(aText) > 8_000_000: raise ValueError('YAML input size limit exceeded')
        data = yaml.load(aText, Loader=_NoDuplicateSafeLoader)
        return AIsSmartDataObjectFactory.fromData(aReadContract, data, aRegistry)

class AIcYamlFormatBindingProvider:
    """Service-discoverable YAML provider without legacy SDO type markers."""
    def getFormatId(self) -> str: return 'YAML'
    def getProviderId(self) -> str: return 'algites-yaml'
    def resolveMetadata(self, aMetadata):
        for owner in aMetadata.getImplementationClass().__mro__:
            schema = owner.__dict__.get('__json_schema__')
            if schema is not None: return _YamlMetadata(schema)
        return None
    def createMarshaller(self): return _Marshaller()
    def createUnmarshaller(self): return _Unmarshaller()
