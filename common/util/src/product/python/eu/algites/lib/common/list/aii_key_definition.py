"""Typed key-definition protocol for multi-mapped lists.

Author: linhart1.
"""

from __future__ import annotations

from typing import Protocol, TypeVar

V = TypeVar("V")
K = TypeVar("K")


class AIiKeyDefinition(Protocol[V, K]):
    """A uniquely registered, pure projection from a stored value to a key.

    Key objects must be hashable and their equality/hash behavior must remain
    constant during membership. A definition's identity, not just its ``id``,
    determines which map view it selects. The ``unique`` flag controls whether
    two entries may share the same extracted key.
    """

    @property
    def id(self) -> str:
        """Return the nonempty diagnostic identifier of the key definition."""
        ...

    @property
    def unique(self) -> bool:
        """Return whether the definition rejects duplicate extracted keys."""
        ...

    def extract_key(self, value: V) -> K:
        """Extract a non-None, stable, hashable key from ``value``.

        Args:
            value: Non-None stored value.

        Returns:
            Key of type ``K``.

        Raises:
            Exception: Propagates extraction failures to the calling mutation.
        """
        ...
