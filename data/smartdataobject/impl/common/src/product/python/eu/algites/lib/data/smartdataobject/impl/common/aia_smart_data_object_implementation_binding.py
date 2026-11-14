"""Class-level explicit read-only contract binding for SDO implementations."""
from __future__ import annotations
from dataclasses import dataclass

@dataclass(frozen=True)
class AIaSmartDataObjectImplementationBinding:
    """Declare multiple read-only contracts; inherited declarations accumulate."""
    readContracts: tuple[type, ...]

    def __call__(self, cls: type) -> type:
        """Record only the current class's declarations without touching its ancestors."""
        if not isinstance(cls, type):
            raise TypeError('Implementation binding requires a class')
        if not self.readContracts or not all(isinstance(c, type) for c in self.readContracts):
            raise ValueError('readContracts must contain actual contract classes')
        cls.__sdo_implementation_binding__ = self
        return cls
