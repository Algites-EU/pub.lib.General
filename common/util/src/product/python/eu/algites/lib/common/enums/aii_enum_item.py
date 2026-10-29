from typing import Protocol, runtime_checkable

@runtime_checkable
class AIiEnumItem(Protocol):
    def code(self) -> str: ...
