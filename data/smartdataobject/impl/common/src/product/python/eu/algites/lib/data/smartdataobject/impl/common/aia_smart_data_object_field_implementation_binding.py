"""Getter-specific explicit implementation binding for SDO deserialization."""
from __future__ import annotations
from dataclasses import dataclass

@dataclass(frozen=True)
class AIaSmartDataObjectFieldImplementationBinding:
    """Identify a concrete implementation override for an SDO-valued field."""
    implementationClass: type

    def __call__(self, getter):
        """Attach this declaration to a getter without modifying its behavior."""
        if not callable(getter) or not isinstance(self.implementationClass, type):
            raise TypeError('Implementation binding requires a getter and class')
        getter.__sdo_field_implementation_binding__ = self
        return getter
