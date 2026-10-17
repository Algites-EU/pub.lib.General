from eu.algites.lib.factory.cpdiscovery.aix_category_provider_discovery_exception import AIxCategoryProviderDiscoveryException


class AIxCategoryProviderUnavailableException(AIxCategoryProviderDiscoveryException):
    """No unambiguous matching provider is registered."""
