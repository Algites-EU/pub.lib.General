"""Marshal a concrete SDO graph using a purpose-scoped binding registry."""
from __future__ import annotations
from typing import Protocol

class AIiSmartDataObjectMarshaller(Protocol):
    """Transform a typed SDO instance into one supported external representation."""
    def marshal(self, aObject: object, aRegistry: object) -> str: ...
