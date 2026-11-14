"""JSON format provider for the independent common SDO implementation."""
from __future__ import annotations
from dataclasses import dataclass
from typing import Any
import json
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_data_tree import AIsSmartDataObjectDataTree
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_factory import AIsSmartDataObjectFactory

@dataclass(frozen=True)
class _JsonMetadata:
    """Format-specific JSON Schema contribution for one SDO implementation."""
    definition: Any
    def getFormatId(self) -> str: return 'JSON'
    def getProviderId(self) -> str: return 'algites-json'

class _Marshaller:
    """JSON serializer with canonical schema data-field names."""
    def marshal(self, aObject, aRegistry) -> str:
        return json.dumps(AIsSmartDataObjectDataTree.toData(aObject, aRegistry),
                          ensure_ascii=False, separators=(',', ':'), allow_nan=False)

class _Unmarshaller:
    """JSON parser with duplicate-member detection."""
    def unmarshal(self, aText: str, aReadContract: type, aRegistry):
        if len(aText) > 8_000_000: raise ValueError('JSON input size limit exceeded')
        def unique(pairs):
            result = {}
            for key, value in pairs:
                if key in result: raise ValueError('Duplicate JSON property: ' + key)
                result[key] = value
            return result
        data = json.loads(aText, object_pairs_hook=unique,
                          parse_constant=lambda value: (_ for _ in ()).throw(ValueError('Non-finite JSON number')))
        return AIsSmartDataObjectFactory.fromData(aReadContract, data, aRegistry)

class AIcJsonFormatBindingProvider:
    """Service-discoverable JSON provider; does not depend on legacy runtime."""
    def getFormatId(self) -> str: return 'JSON'
    def getProviderId(self) -> str: return 'algites-json'
    def resolveMetadata(self, aMetadata):
        for owner in aMetadata.getImplementationClass().__mro__:
            schema = owner.__dict__.get('__json_schema__')
            if schema is not None: return _JsonMetadata(schema)
        return None
    def createMarshaller(self): return _Marshaller()
    def createUnmarshaller(self): return _Unmarshaller()
