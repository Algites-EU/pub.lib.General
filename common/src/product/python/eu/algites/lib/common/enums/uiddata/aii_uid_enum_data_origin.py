from typing import Protocol
from eu.algites.lib.common.enums.aii_enum_item import AIiEnumItem
class AIiUidEnumDataOrigin(AIiEnumItem, Protocol):
    def namespaceUsed(self) -> bool: ...
