class AIxCategoryProviderDiscoveryException(RuntimeError):
    """Provider discovery or creation failed."""


class AIxCategoryProviderConflictException(AIxCategoryProviderDiscoveryException):
    """Conflicting provider identity or default configuration."""


class AIxCategoryProviderUnavailableException(AIxCategoryProviderDiscoveryException):
    """No unambiguous matching provider is registered."""
