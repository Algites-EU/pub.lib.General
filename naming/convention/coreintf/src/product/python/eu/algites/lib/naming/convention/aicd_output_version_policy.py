from __future__ import annotations

from dataclasses import dataclass

@dataclass(frozen=True, slots=True)
class AIcdOutputVersionPolicy:
    """Carries immutable output version policy data.

    Attributes:
        include_version: Whether to render the canonical version.
        separator: Version separator.
        prefix: Literal name prefix.
        suffix: Literal name suffix.
    """
    include_version: bool = True
    separator: str = "_"
    prefix: str = ""
    suffix: str = ""
