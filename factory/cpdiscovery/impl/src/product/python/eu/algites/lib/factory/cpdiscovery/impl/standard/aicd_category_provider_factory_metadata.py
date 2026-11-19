"""Immutable registry metadata; inspection never creates a provider instance."""
from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class AIcdCategoryProviderFactoryMetadata:
    """One specialized factory interface/category/provider registration."""
    category: str
    provider_id: str
    implementation_interface_type: type
    context_interface_type: type
    default_provider: bool
    source: str
