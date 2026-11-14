"""Public mutable list API with non-worsening cardinality validation.

A newly created list need not satisfy its minimum; it must be possible to build
it incrementally. Complete validity is queried explicitly with isValid().
"""
from __future__ import annotations
from abc import abstractmethod
from collections.abc import Callable, MutableSequence
from typing import Generic, TypeVar
from eu.algites.lib.common.validation.aii_validatable import AIiValidatable
from .aicd_bounded_list_validation_issue import AIcdBoundedListValidationIssue
from .ain_bounds_mutation_policy import AInBoundsMutationPolicy

T = TypeVar('T')

class AIiBoundedList(MutableSequence[T], AIiValidatable[AIcdBoundedListValidationIssue], Generic[T]):
    """List with optional inclusive bounds and a selected mutation policy."""

    @abstractmethod
    def getMinSize(self) -> int | None: ...

    @abstractmethod
    def getMaxSize(self) -> int | None: ...

    @abstractmethod
    def getMutationPolicy(self) -> AInBoundsMutationPolicy: ...

    @abstractmethod
    def modifyAtomically(self, aChange: Callable[[list[T]], object]) -> None:
        """Atomically publish a candidate if its constraint violation does not worsen."""
