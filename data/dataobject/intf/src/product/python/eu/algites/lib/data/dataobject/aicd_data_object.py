"""Metadata for a normalized data object contract."""
from dataclasses import dataclass
from .ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy

@dataclass(frozen=True, slots=True)
class AIcdDataObject:
    """Describes a source-independent contract and optional instance metadata mapping.

    Schema identifiers are stored in the JSON/YAML format annotations,
    not in this technology-independent declaration.
    """
    title: str = ''
    description: str = ''
    setter_inheritance_policy: AInFieldSetterInheritancePolicy = AInFieldSetterInheritancePolicy.DEFAULT
