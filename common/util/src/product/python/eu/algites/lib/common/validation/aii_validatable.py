from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Generic, TypeVar
from eu.algites.lib.common.validation.aii_validation_issue import AIiValidationIssue

T = TypeVar('T', bound=AIiValidationIssue)

class AIiValidatable(ABC, Generic[T]):
    """A value that can be temporarily invalid while being assembled."""
    @abstractmethod
    def isValid(self) -> bool: ...

    @abstractmethod
    def getValidationIssues(self) -> list[T]: ...

    def getInvalidStateMessage(self, aLocale: str | None) -> str:
        return '\n'.join(((issue.getPath() + ': ') if issue.getPath() else '') + issue.getMessage(aLocale)
                         for issue in self.getValidationIssues())
