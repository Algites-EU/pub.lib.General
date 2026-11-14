"""Entry-point discovery for independently packaged format binding providers."""
from __future__ import annotations
from importlib.metadata import entry_points
from threading import RLock
from typing import Any

class AIsSmartDataObjectFormatBindingProviderRegistry:
    """Discover all visible providers; no per-WS-client partitioning is imposed."""
    _lock = RLock()
    _explicit: dict[type, object] = {}
    # One entry-point lookup per interpreter process, including an empty result.
    # Explicit providers are kept separately so explicit registration never
    # triggers distribution discovery or creates another service instance.
    _discovered: tuple[object, ...] | None = None
    ENTRY_POINT_GROUP = 'eu.algites.lib.data.smartdataobject.format_binding_providers'

    @classmethod
    def registerProvider(cls, provider: object) -> None:
        """Register a provider explicitly, preserving its concrete class identity."""
        if provider is None:
            raise TypeError('provider is required')
        with cls._lock:
            cls._explicit.setdefault(type(provider), provider)

    @classmethod
    def getProviders(cls, aFormatId: str | None = None) -> tuple[object, ...]:
        """Return providers from one process-wide entry-point discovery.

        Entry points (including a negative result) are scanned and instantiated
        once on the first request. Explicitly registered providers are merged
        without rescanning packages; callers should register them before
        creating metadata that consumes their contributions.

        Args:
            aFormatId: Optional stable format code used to filter cached services.

        Returns:
            All matching provider instances, shared by identity across calls.

        Raises:
            Any exception raised by a malformed entry point or its constructor;
            unsuccessful discovery is not cached and can be retried.
        """
        with cls._lock:
            if cls._discovered is None:
                # Keep the lock through discovery to avoid duplicate providers
                # when multiple threads access metadata concurrently.
                discovered: dict[type, object] = {}
                for ep in entry_points(group=cls.ENTRY_POINT_GROUP):
                    factory = ep.load()
                    if isinstance(factory, type):
                        # Duplicate entry-point declarations must not create two
                        # instances of the very same provider implementation.
                        if factory in discovered or factory in cls._explicit:
                            continue
                        provider = factory()
                    else:
                        provider = factory
                    discovered.setdefault(type(provider), provider)
                cls._discovered = tuple(discovered.values())
            available = dict(cls._explicit)
            for provider in cls._discovered:
                available.setdefault(type(provider), provider)
            providers = tuple(available.values())
        return tuple(provider for provider in providers
                     if aFormatId is None or provider.getFormatId() == aFormatId)
