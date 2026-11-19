"""Configurable registry bridge for annotation-selected factories."""
from .aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from .aix_category_provider_unavailable_exception import AIxCategoryProviderUnavailableException
from .aii_category_provider_factory_metadata import AIiCategoryProviderFactoryMetadata

class AIsCategoryProviderDiscovery:
    """Selects specialized resolver providers through an application-installed registry."""
    _registry = None

    @classmethod
    def set_registry(cls, registry):
        """Install a registry with discovered/registered application providers, or clear it."""
        cls._registry = registry

    @classmethod
    def get_instance(cls, definition: AIaCategoryProviderFactoryDefinition,
                     expected_factory_class: type, default_category: str, context=None):
        """Resolve an annotation selection; fail on missing or mismatched factories."""
        if not issubclass(definition.factory_class, expected_factory_class):
            raise ValueError('Unexpected specialized factory interface')
        if cls._registry is None:
            raise AIxCategoryProviderUnavailableException('No CategoryProvider registry configured')
        return cls._registry.get_instance(definition.factory_class,
                                         definition.factory_category or default_category,
                                         context, definition.factory_provider_id or None)

    @classmethod
    def find_factory(cls, factory_class: type, category: str, provider_ids=None):
        """Find a registered factory without constructing its implementation."""
        if cls._registry is None:
            raise AIxCategoryProviderUnavailableException('No CategoryProvider registry configured')
        return cls._registry.find_factory(factory_class, category, provider_ids)

    @classmethod
    def get_available_factory_metadata(cls, factory_class: type) -> tuple[AIiCategoryProviderFactoryMetadata, ...]:
        """Return all registered provider/category metadata for a specialized SPI."""
        if cls._registry is None:
            raise AIxCategoryProviderUnavailableException('No CategoryProvider registry configured')
        return cls._registry.get_available_factory_metadata(factory_class)

    @classmethod
    def get_available_categories(cls, factory_class: type):
        """Return registered category IDs."""
        if cls._registry is None:
            raise AIxCategoryProviderUnavailableException('No CategoryProvider registry configured')
        return cls._registry.get_available_categories(factory_class)

    @classmethod
    def get_available_provider_ids(cls, factory_class: type, category: str):
        """Return available provider IDs for a category."""
        if cls._registry is None:
            raise AIxCategoryProviderUnavailableException('No CategoryProvider registry configured')
        return cls._registry.get_available_provider_ids(factory_class, category)
