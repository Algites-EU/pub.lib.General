"""Unmarshal an external document using a purpose-scoped binding registry."""
from __future__ import annotations
from typing import Protocol

class AIiSmartDataObjectUnmarshaller(Protocol):
    """Restore an object graph, resolving implementation classes from a registry."""
    def unmarshal(self, aText: str, aReadContract: type, aRegistry: object) -> object: ...
