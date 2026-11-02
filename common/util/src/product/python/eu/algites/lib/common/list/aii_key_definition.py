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

    @property
    def required(self) -> bool:
        """Whether a missing/None key makes an insertion invalid."""
        ...

    def extract_key(self, value: V) -> K | None:
        """Extract a stable key, or None for a missing optional key.

        Args:
            value: Non-None stored value.

        Returns:
            Key of type ``K``, or None if the definition is optional and a
            complete key cannot be extracted.

        Raises:
            Exception: Propagates extraction failures to the calling mutation.
        """
        ...
