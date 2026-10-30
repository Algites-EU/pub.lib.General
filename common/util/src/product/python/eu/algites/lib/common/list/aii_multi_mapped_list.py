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

V = TypeVar('V')
K = TypeVar('K')


class AIiMultiMappedList(MutableSequence[V]):
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
