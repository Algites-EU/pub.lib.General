from __future__ import annotations
from dataclasses import dataclass
from threading import RLock
from eu.algites.lib.common.enums.uiddata.ais_uid_enum_data_utils import AIsUidEnumDataUtils
from eu.algites.lib.common.enums.uiddata.aix_enum_data_registry_illegal_operation_exception import AIxEnumDataRegistryIllegalOperationException
from eu.algites.lib.common.enums.uiddata.aix_enum_data_registry_item_not_found_exception import AIxEnumDataRegistryItemNotFoundException

@dataclass
class _Entry:
    data: object
    read_only: bool
    permanent: bool

class AIcUidEnumDataRegistry:
    """Thread-safe identity-keyed singleton UID registry, like its Java counterpart."""
    _instance = None
    _singleton_lock = RLock()
    def __init__(self):
        self._lock = RLock()
        self._entries: dict[int, tuple[object,dict[str,_Entry]]] = {}

    @classmethod
    def getInstance(cls):
        with cls._singleton_lock:
            if cls._instance is None: cls._instance = cls()
            return cls._instance

    def _items(self, data_type):
        k = id(data_type)
        stored = self._entries.get(k)
        if stored is not None and stored[0] is data_type: return stored[1]
        result = {}
        self._entries[k] = (data_type, result)
        return result

    def registerData(self,aReadOnly,aPermanent,aOverwriteExisting,*aDataToRegister):
        items=[]
        with self._lock:
            for item in aDataToRegister:
                if item is None: continue
                typ=item.getDataType()
                if typ is None: raise ValueError('Data type must not be null')
                uid=item.uid()
                AIsUidEnumDataUtils.validateUid(typ,uid)
                by_uid=self._items(typ)
                old=by_uid.get(uid)
                if old is not None:
                    if old.read_only: raise AIxEnumDataRegistryIllegalOperationException(f'Cannot overwrite read-only item {uid!r}')
                    if not aOverwriteExisting: continue
                by_uid[uid]=_Entry(item,aReadOnly,aPermanent)
                items.append(item)
        return items

    def _release(self,typ,uid):
        AIsUidEnumDataUtils.validateUid(typ,uid)
        entry=self._entries.get(id(typ))
        if not entry or entry[0] is not typ: return None
        found=entry[1].get(uid)
        if found is None: return None
        if found.permanent: raise AIxEnumDataRegistryIllegalOperationException(f'Cannot release permanent item {uid!r}')
        del entry[1][uid]
        return found.data

    def releaseData(self,*args):
        if not args: return []
        # Java overload (data_type, *uid_strings) versus (*data_items).
        if len(args)>=2 and isinstance(args[1],str):
            typ=args[0]
            if typ is None: raise ValueError('Data type must not be null')
            with self._lock: return [x for uid in args[1:] if uid is not None if (x:=self._release(typ,uid)) is not None]
        with self._lock:
            result=[]
            for item in args:
                if item is None: continue
                typ=item.getDataType()
                if typ is None: raise ValueError('Data type must not be null')
                x=self._release(typ,item.uid())
                if x is not None: result.append(x)
            return result

    def findByUid(self,aDataType,aUid):
        if aDataType is None or aUid is None: raise ValueError('Type and UID are required')
        AIsUidEnumDataUtils.validateUid(aDataType,aUid)
        with self._lock:
            entry=self._entries.get(id(aDataType))
            return entry[1][aUid].data if entry and entry[0] is aDataType and aUid in entry[1] else None

    def getOrThrow(self,aDataType,aUid):
        result=self.findByUid(aDataType,aUid)
        if result is None: raise AIxEnumDataRegistryItemNotFoundException(f'Item not found for uid {aUid!r} in data type {aDataType!r}')
        return result

    def items(self,aDataType):
        if aDataType is None: raise ValueError('Data type must not be null')
        with self._lock:
            entry=self._entries.get(id(aDataType))
            return tuple(item.data for item in entry[1].values()) if entry and entry[0] is aDataType else ()
