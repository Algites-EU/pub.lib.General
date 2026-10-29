"""Dedicated CPDiscovery resolver factory interface."""
from abc import ABC
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory import AIiCategoryProviderFactory

class AIiRenderingOutputPurposeResolverFactory(AIiCategoryProviderFactory, ABC):
    """Typed resolver provider factory selected by annotation metadata."""
