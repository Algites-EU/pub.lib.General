from __future__ import annotations

from abc import ABC, abstractmethod
from threading import RLock
from typing import Generic, TypeVar, final

from .aii_category_provider_factory import AIiCategoryProviderFactory

I = TypeVar('I')
C = TypeVar('C')


class AIcCachingCategoryProviderFactory(AIiCategoryProviderFactory[I, C], ABC, Generic[I, C]):
    """Optional per-factory cache for shared instances keyed by category and context.

    Contexts must be hashable and have stable equality/hash semantics while cached;
    ``None`` is supported. A factory returning per-invocation state should not use
    this base. Concurrent requests for the same key create at most one instance.
    Failures and None results are never cached.
    """

    def __init__(self) -> None:
        self._instances: dict[tuple[str, object], I] = {}
        self._cache_lock = RLock()

    @final
    def create_instance(self, category: str, context: C) -> I:
        if not isinstance(category, str) or not category.strip():
            raise ValueError("category must be a nonblank string")
        key = (category, context)
        with self._cache_lock:
            if key not in self._instances:
                instance = self.create_uncached_instance(category, context)
                if instance is None:
                    raise ValueError(f"Factory returned None for {category}")
                self._instances[key] = instance
            return self._instances[key]

    @abstractmethod
    def create_uncached_instance(self, category: str, context: C) -> I:
        """Build a non-None shareable instance on cache miss."""

    def _clear_instance_cache(self) -> None:
        """Invalidate cached instances (does not revoke references already handed out)."""
        with self._cache_lock:
            self._instances.clear()
