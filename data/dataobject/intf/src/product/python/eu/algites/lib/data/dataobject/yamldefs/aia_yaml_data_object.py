"""YAML source schema identity and instance-document metadata."""
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class AIaYamlDataObject:
    """Format-local schema identity; independent of per-class naming conventions."""
    __deprecated__ = 'Deprecated since 1.1: use AIaDataObjectJsonSchema (legacy compatibility retained)'
    schema_id: str = ''
    schema_version: int = -1
    schema_url: str = ''
    data_source_schema_url_field: str = ''
    data_source_schema_identifier_field: str = ''
    data_source_schema_version_field: str = ''
