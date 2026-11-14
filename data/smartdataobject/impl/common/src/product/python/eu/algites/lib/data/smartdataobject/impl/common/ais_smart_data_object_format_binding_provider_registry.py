"""Entry-point discovery for independently packaged format binding providers."""
from __future__ import annotations
from importlib.metadata import entry_points
from threading import RLock
from typing import Any

class AIsSmartDataObjectFormatBindingProviderRegistry:
    """Discover all visible providers; no per-WS-client partitioning is imposed."""
    _lock = RLock()
    _explicit: dict[type, object] = {}
    ENTRY_POINT_GROUP = 'eu.algites.lib.data.smartdataobject.format_binding_providers'

    @classmethod
    def registerProvider(cls, provider: object) -> None:
        """Register a provider explicitly, preserving its concrete class identity."""
        if provider is None:
            raise TypeError('provider is required')
        with cls._lock:
            cls._explicit[type(provider)] = provider

    @classmethod
    def getProviders(cls, aFormatId: str | None = None) -> tuple[object, ...]:
        """Instantiate all visible providers, optionally filtering by external format code."""
        with cls._lock:
            available = dict(cls._explicit)
        for ep in entry_points(group=cls.ENTRY_POINT_GROUP):
            factory = ep.load()
            provider = factory() if isinstance(factory, type) else factory
            available.setdefault(type(provider), provider)
        return tuple(p for p in available.values()
                     if aFormatId is None or p.getFormatId() == aFormatId)
