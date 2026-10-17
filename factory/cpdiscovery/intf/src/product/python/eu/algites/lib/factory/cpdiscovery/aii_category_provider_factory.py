from __future__ import annotations
from typing import Generic, TypeVar
from abc import ABC, abstractmethod

I = TypeVar('I')
C = TypeVar('C')


class AIiCategoryProviderFactory(ABC, Generic[I, C]):
    @abstractmethod
    def get_implementation_interface_type(self) -> type[I]: ...

    @abstractmethod
    def get_context_interface_type(self) -> type[C]: ...

    @abstractmethod
    def create_instance(self, category: str, context: C) -> I: ...
