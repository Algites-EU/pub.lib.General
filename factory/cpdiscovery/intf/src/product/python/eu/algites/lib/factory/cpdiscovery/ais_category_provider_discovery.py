"""Configurable registry bridge for annotation-selected factories."""
from .aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from .aix_category_provider_unavailable_exception import AIxCategoryProviderUnavailableException

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
