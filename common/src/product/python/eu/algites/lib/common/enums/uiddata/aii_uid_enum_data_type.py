from typing import Protocol, Callable, Any
from eu.algites.lib.common.enums.uiddata.ain_uid_enum_data_origin import AInUidEnumDataOrigin
class AIiUidEnumDataType(Protocol):
    def getUidRecordFactory(self) -> Callable[[str,list[str]],Any]: ...
    def getSpecificUidPartsMetadata(self) -> list: ...
    def getOriginGetter(self): return AInUidEnumDataOrigin.getByCodeOrThrow
    def getOriginFinder(self): return AInUidEnumDataOrigin.findByCodeOrNull
    def getUidPartCount(self) -> int: return len(self.getSpecificUidPartsMetadata()) + 2
