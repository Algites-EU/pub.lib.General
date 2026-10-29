from typing import Protocol, Any
class AIiUidEnumData(Protocol):
    def getDataType(self) -> Any: ...
    def uid(self) -> str: ...
    def getDataRecord(self):
        from eu.algites.lib.common.enums.uiddata.ais_uid_enum_data_utils import AIsUidEnumDataUtils
        return AIsUidEnumDataUtils.parseUid(self.getDataType(), self.uid())
