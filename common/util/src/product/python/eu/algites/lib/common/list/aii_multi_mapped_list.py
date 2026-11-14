"""Interface contract for ordered multi-key mapped lists.

The list is the source of truth. Every registered key definition exposes one
cached, live, deletion-only map. Its grouped values are immutable snapshots.
Sublist membership is tracked and parent removals propagate; unrelated parent
insertions do not change existing sublist membership. No list implementation
may promise consistency if a stored value's key attributes are mutated outside
its list operations.

Author: linhart1.
"""
from __future__ import annotations

from collections.abc import MutableMapping, MutableSequence
from abc import abstractmethod
from typing import TypeVar

from .aii_key_definition import AIiKeyDefinition
from .aii_bounded_list import AIiBoundedList

V = TypeVar('V')
K = TypeVar('K')


class AIiMultiMappedList(AIiBoundedList[V]):
    """Ordered mutable sequence with cached, deletion-only multi-key maps.

    A failed insertion or replacement must leave all maps and list membership
    unchanged. Concurrent writes and post-insertion edits of key components
    are outside this contract.
    """

    @abstractmethod
    def map_by_key_type(self, definition: AIiKeyDefinition[V, K]) -> MutableMapping[K, tuple[V, ...]]:
        """Return the same live map for every lookup of a registered definition.

        Args:
            definition: Registered key extractor, identified by object identity.

        Returns:
            A live mapping from keys to immutable tuple snapshots. Deleting a
            key removes every matching occurrence from all list/map views.

        Raises:
            ValueError: The supplied definition is not registered.
        """
        ...

    @abstractmethod
    def key_definition(self, identifier: str) -> AIiKeyDefinition[V, object] | None:
        """Return the registered index definition with the given identity.

        Args:
            identifier: Stable schema index name.

        Returns:
            Exact identity-bearing definition object, or None if not registered.
        """
        ...


    @abstractmethod
    def get_key_count(self) -> int:
        """Return the number of registered key definitions."""
        ...

    def is_one_key_list(self) -> bool:
        """Return whether exactly one key definition is registered."""
        return self.get_key_count() == 1

    @abstractmethod
    def is_dynamic_key_defs_list(self) -> bool:
        """Return whether key definitions may change after construction."""
        ...

    @abstractmethod
    def get_one_key_map(self) -> MutableMapping[object, tuple[V, ...]]:
        """Return the registered key map when exactly one definition exists."""
        ...

    @abstractmethod
    def add_key_definition(self, definition: AIiKeyDefinition[V, object]) -> None:
        """Register a definition atomically, or reject a fixed-definition list."""
        ...

    @abstractmethod
    def remove_key_definition(self, identifier: str) -> bool:
        """Remove a registered definition without deleting list elements."""
        ...

    @abstractmethod
    def clear_key_definitions(self) -> None:
        """Remove all key definitions while preserving list elements."""
        ...

    def map_by_key_id(self, identifier: str) -> MutableMapping[object, tuple[V, ...]]:
        """Look up a named live key-to-group map."""
        definition = self.key_definition(identifier)
        if definition is None:
            raise ValueError('Unknown key definition: ' + identifier)
        return self.map_by_key_type(definition)

    @abstractmethod
    def copy_range(self, start: int, stop: int) -> AIiMultiMappedList[V]:
        """Create an independently mutable shallow copy of the given range.

        Args:
            start: Inclusive start index.
            stop: Exclusive stop index.

        Returns:
            A new mapped list with independent membership and copied indexes;
            the stored value instances remain shared.

        Raises:
            IndexError: Range indices are invalid.
        """
        ...

    @abstractmethod
    def sub_list(self, start: int, stop: int) -> MutableSequence[V]:
        """Return a weakly tracked membership-backed mutable subsequence.

        Args:
            start: Inclusive start index.
            stop: Exclusive stop index.

        Returns:
            Live membership view whose removals affect all indexes and whose
            membership is unaffected by unrelated parent insertions.

        Raises:
            IndexError: Range indices are invalid.
        """
        ...
