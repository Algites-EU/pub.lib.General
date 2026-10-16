"""Four unambiguously named Algites schema identifiers."""
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class AIcdSchemaIdentity:
    """SchemaId and SchemaVersion form VersionedSchemaId; SchemaUrl is separate.

    SchemaUrl identifies one published representation and may be absent. None
    of these properties is an object-instance ID or a JSON Schema dialect URL.
    """
    schema_id: str
    schema_version: int
    schema_url: str = ''

    def __post_init__(self):
        if not self.schema_id or self.schema_version <= 0:
            raise ValueError('SchemaId and positive SchemaVersion are required')

    @property
    def versioned_schema_id(self) -> str:
        """Return the logical schema identity with version, not a URL."""
        return f'{self.schema_id}:{self.schema_version}'
