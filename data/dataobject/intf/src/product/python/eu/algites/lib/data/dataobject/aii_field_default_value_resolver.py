"""Default field value interface."""
from typing import Protocol, Any

class AIiFieldDefaultValueResolver(Protocol):
    """Resolves a default without modifying field raw presence."""
    def resolve_default(self, owner_class: type, getter_name: str) -> Any: ...
