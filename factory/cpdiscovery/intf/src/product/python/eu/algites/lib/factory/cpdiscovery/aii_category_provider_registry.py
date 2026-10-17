from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Any


class AIiCategoryProviderRegistry(ABC):
    @abstractmethod
    def get_instance(self, factory_type: type, category: str, context: Any,
                     provider_id: str | None = None) -> Any: ...

    @abstractmethod
    def register_entry_point_factories(self) -> None: ...

    @abstractmethod
    def unregister_entry_point_factories(self) -> None: ...
