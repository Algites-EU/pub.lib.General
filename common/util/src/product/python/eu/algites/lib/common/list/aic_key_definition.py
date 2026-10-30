"""Immutable function-backed key definition for multi-mapped lists.

Author: linhart1.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Callable, Generic, TypeVar

V = TypeVar("V")
K = TypeVar("K")


@dataclass(frozen=True, eq=False)
class AIcKeyDefinition(Generic[V, K]):
    """An immutable definition identified by object identity, not by value.

    Attributes:
        id: Nonempty string identifier, unique among a list's definitions.
        extractor: Pure callable that returns a hashable, non-None key.
        unique: True for unique map indexes, False for grouping indexes.

    Raises:
        ValueError: At construction if ``id`` is empty or whitespace.
        TypeError: At construction if the extractor is not callable.
    """

    id: str
    extractor: Callable[[V], K]
    unique: bool

    def __post_init__(self) -> None:
        """Validate the identifier and callable after dataclass initialization.

        Raises:
            ValueError: If the identifier is empty or whitespace.
            TypeError: If the extractor is not callable or id is not text.
        """
        if not isinstance(self.id, str):
            raise TypeError("Key definition id must be a string")
        if not self.id.strip():
            raise ValueError("Key definition id must not be empty")
        if not callable(self.extractor):
            raise TypeError("Key extractor must be callable")

    def extract_key(self, value: V) -> K:
        """Apply the extractor to a value without altering it.

        Args:
            value: Non-None value from the owning list.

        Returns:
            The extracted key.

        Raises:
            Exception: Any exception raised by the supplied extractor.
        """
        return self.extractor(value)
