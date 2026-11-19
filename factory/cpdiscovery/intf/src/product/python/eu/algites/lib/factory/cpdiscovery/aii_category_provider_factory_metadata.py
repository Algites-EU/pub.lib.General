"""Public, immutable-view contract for registered CPDiscovery factory metadata."""
from typing import Protocol, runtime_checkable


@runtime_checkable
class AIiCategoryProviderFactoryMetadata(Protocol):
    """Read-only metadata attributes independent of the concrete representation.

    Implementations provide a frozen data object. Python's structural protocols
    avoid coupling this public contract to the implementation distribution.
    """

    @property
    def category(self) -> str: ...

    @property
    def provider_id(self) -> str: ...

    @property
    def implementation_interface_type(self) -> type: ...

    @property
    def context_interface_type(self) -> type: ...

    @property
    def default_provider(self) -> bool: ...

    @property
    def source(self) -> str: ...
