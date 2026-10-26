"""Metadata for a normalized data object contract."""
from dataclasses import dataclass
from .ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy

@dataclass(frozen=True, slots=True)
class AIcdDataObject:
    """Describes a source-independent contract and optional instance metadata mapping.

    The schema URL is separate from its stable logical ID and version. Empty
    ``data_source_schema_*_field`` names disable output of the corresponding
    value into JSON/YAML data; a disabled output does not erase the annotated
    source metadata. Nonempty names originate in the selected input convention.
    """
    schema_id: str = ''
    schema_version: int = -1
    schema_url: str = ''
    data_source_schema_url_field: str = ''
    data_source_schema_identifier_field: str = ''
    data_source_schema_version_field: str = ''
    title: str = ''
    description: str = ''
    fingerprint: str = ''
    naming_convention: str = ''
    setter_inheritance_policy: AInFieldSetterInheritancePolicy = AInFieldSetterInheritancePolicy.DEFAULT
