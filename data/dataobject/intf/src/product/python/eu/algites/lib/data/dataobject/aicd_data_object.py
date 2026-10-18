"""Metadata for a normalized data object contract."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIcdDataObject:
    """Describes a data contract independently of its source or runtime implementation."""
    schema_id: str = ''
    schema_version: int = -1
    schema_url: str = ''
    title: str = ''
    description: str = ''
    local_name: str = ''
    namespace: str = ''
    fingerprint: str = ''
    source_contract: type | None = None
