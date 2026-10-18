"""Annotation-equivalent immutable definition of CPDiscovery factory selection."""
from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIaCategoryProviderFactoryDefinition:
    """Lookup by specialized factory SPI, category and optional provider identifier."""
    factory_class: type
    factory_category: str = ''
    factory_provider_id: str = ''
