from __future__ import annotations
from collections.abc import Callable, Mapping, Sequence
from eu.algites.lib.common.enums.uiddata.ain_uid_enum_data_origin import AInUidEnumDataOrigin
class AIrUidPartMetadata:
    __slots__ = ('_supplier', '_required')
    def __init__(self, aDisplayLabelSupplier: Callable[[], str],
                 aOriginValuesOrMap: Mapping | Sequence | bool, aRequiredForAllItems: bool | None = None):
        self._supplier = aDisplayLabelSupplier
        if isinstance(aOriginValuesOrMap, bool) and aRequiredForAllItems is None:
            self._required = {o: aOriginValuesOrMap for o in AInUidEnumDataOrigin}
        elif isinstance(aOriginValuesOrMap, Mapping) and aRequiredForAllItems is None:
            self._required = dict(aOriginValuesOrMap)
        else:
            self._required = {o: aRequiredForAllItems for o in aOriginValuesOrMap}
    def displayLabelSupplier(self): return self._supplier
    def requiredForOrigin(self): return dict(self._required)
    def __eq__(self, x): return isinstance(x, AIrUidPartMetadata) and self._supplier == x._supplier and self._required == x._required
    def __hash__(self): return hash((self._supplier, frozenset(self._required.items())))
