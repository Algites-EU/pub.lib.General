"""Metadata for a normalized data object contract."""
from dataclasses import dataclass
from .ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy

@dataclass(frozen=True, slots=True)
class AIcdDataObject:
    """Describes a data contract independently of its source or runtime implementation."""
    schema_id: str = ''
    schema_version: int = -1
    schema_url: str = ''
    title: str = ''
    description: str = ''
    fingerprint: str = ''
    naming_convention: str = ''
    setter_inheritance_policy: AInFieldSetterInheritancePolicy = AInFieldSetterInheritancePolicy.DEFAULT
