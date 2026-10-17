from __future__ import annotations
from eu.algites.lib.common.enums.uiddata.ain_uid_enum_data_origin import AInUidEnumDataOrigin

class AIrUidPartsRecord:
    """Immutable Java-record-style carrier for origin and namespace."""
    __slots__ = ('_origin','_namespace')
    def __init__(self, aOrigin: AInUidEnumDataOrigin, aNamespace: str):
        object.__setattr__(self,'_origin',aOrigin)
        object.__setattr__(self,'_namespace',aNamespace)
    def __setattr__(self, key, value): raise AttributeError('UID parts record is immutable')
    def origin(self) -> AInUidEnumDataOrigin: return self._origin
    def namespace(self) -> str: return self._namespace
    def __eq__(self, other): return isinstance(other,AIrUidPartsRecord) and (self._origin,self._namespace)==(other._origin,other._namespace)
    def __hash__(self): return hash((self._origin,self._namespace))
    def __repr__(self): return f'AIrUidPartsRecord(origin={self._origin!r}, namespace={self._namespace!r})'
