"""Specialized CPDiscovery SPI for field default resolvers."""
from abc import ABC
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory import AIiCategoryProviderFactory

class AIiFieldDefaultValueResolverFactory(AIiCategoryProviderFactory, ABC):
    """Factory category for constructing default value resolvers."""
