from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Any
from .aii_category_provider_factory_metadata import AIiCategoryProviderFactoryMetadata


class AIiCategoryProviderRegistry(ABC):
    @abstractmethod
    def get_instance(self, factory_type: type, category: str, context: Any,
                     provider_id: str | None = None) -> Any: ...

    @abstractmethod
    def find_factory(self, factory_type: type, category: str,
                     provider_ids: tuple[str, ...] | list[str] | None = None) -> Any | None:
        """Return a matching factory or None, trying provider IDs in order."""

    @abstractmethod
    def get_available_categories(self, factory_type: type) -> frozenset[str]:
        """Return the categories supported by the specialized factory interface."""

    @abstractmethod
    def get_available_provider_ids(self, factory_type: type, category: str) -> frozenset[str]:
        """Return registered providers for the interface/category pair."""

    @abstractmethod
    def get_available_factory_metadata(self, factory_type: type) -> tuple[AIiCategoryProviderFactoryMetadata, ...]:
        """Return immutable metadata ordered by category and provider identifier."""

    def has_category(self, factory_type: type, category: str) -> bool:
        """Check whether any provider advertises the category."""
        return bool(self.get_available_provider_ids(factory_type, category))

    def has_provider(self, factory_type: type, category: str, provider_id: str) -> bool:
        """Check whether a given provider advertises the category."""
        return provider_id in self.get_available_provider_ids(factory_type, category)

    @abstractmethod
    def register_entry_point_factories(self) -> None: ...

    @abstractmethod
    def unregister_entry_point_factories(self) -> None: ...
