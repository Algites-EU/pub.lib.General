"""Nested default declaration, including XML Schema fixed restrictions."""
from dataclasses import dataclass
from .ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldDefault:
    """Schema lexical default. ``defined=True`` distinguishes an empty value from absence."""
    kind: AInDataObjectFieldDefaultKind = AInDataObjectFieldDefaultKind.DECLARATION_ONLY
    value: str = ''
    defined: bool = False
