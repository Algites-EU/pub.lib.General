"""Factory-backed default-value resolver selection for data-object getters."""
from dataclasses import dataclass, field
from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from .aii_field_default_value_resolver_factory import AIiFieldDefaultValueResolverFactory

@dataclass(frozen=True, slots=True)
class AIaFieldDefault:
    """Selects a field default resolver through the category/provider registry.

    The normalized descriptor does not treat the returned value as explicitly present.
    """
    default_value_resolver: AIaCategoryProviderFactoryDefinition = field(
        default_factory=lambda: AIaCategoryProviderFactoryDefinition(AIiFieldDefaultValueResolverFactory))

    def __call__(self, getter):
        """Attach resolver selection to a Python data-object contract getter."""
        setattr(getter, '__field_default__', self)
        return getter
