"""JSON Schema identity shared by JSONDEFS and YAMLDEFS DataObjects."""
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class AIaDataObjectJsonSchema:
    """Immutable schema identity and optional instance-document metadata."""
    schema_id: str = ''
    schema_version: int = -1
    schema_url: str = ''
    data_source_schema_url_field: str = ''
    data_source_schema_identifier_field: str = ''
    data_source_schema_version_field: str = ''
