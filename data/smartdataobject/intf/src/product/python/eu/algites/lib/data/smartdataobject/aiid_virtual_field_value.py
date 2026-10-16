"""Uniform typed node of an identity-preserving SmartDataObject virtual graph."""
from dataclasses import dataclass
from typing import Any


@dataclass(eq=False, slots=True)
class AIidVirtualFieldValue:
    """Carry a concrete runtime class and representation without confusing them.

    SmartDataObjects are represented by field-name dictionaries containing other
    virtual values; containers contain virtual values too. Node objects may be
    shared to express aliases and cycles. Not a JSON/YAML/XML wire encoding.
    """
    value_class: type
    value_representation: Any = None

    def getValueRepresentation(self) -> Any:
        """Return the representation, not the original DTO instance."""
        return self.value_representation

    def getValueClass(self) -> type:
        """Return the concrete runtime class, not a field's declared interface."""
        return self.value_class
