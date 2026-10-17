from __future__ import annotations
from abc import ABC, abstractmethod

class AIiValidatable(ABC):
    """Value that may be temporarily invalid while being assembled."""
    @abstractmethod
    def isValid(self) -> bool: ...

    @abstractmethod
    def getInvalidStateMessage(self, aLocale: str | None) -> str: ...
