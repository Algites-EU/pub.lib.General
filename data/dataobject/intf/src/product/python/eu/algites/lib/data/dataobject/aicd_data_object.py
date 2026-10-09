"""Metadata for a normalized data object contract."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIcdDataObject:
    """Describes a data contract independently of its source or runtime implementation."""
    id: str
    version: int = -1
    title: str = ''
    description: str = ''
    xml_local_name: str = ''
    xml_namespace: str = ''
    fingerprint: str = ''
