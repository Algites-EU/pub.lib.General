from __future__ import annotations

from dataclasses import dataclass
from eu.algites.lib.naming.convention.ain_version_source import AInVersionSource

@dataclass(frozen=True, slots=True)
class AIcdInputVersionPolicy:
    """Carries immutable input version policy data.

    Attributes:
        source: Version-source strategy.
        file_name_separator: Separator used before a filename version.
        require_positive_integer: Whether versions must be positive integers.
        require_matching_sources: Whether independent version sources must agree.
    """
    source: AInVersionSource
    file_name_separator: str = "_"
    require_positive_integer: bool = True
    require_matching_sources: bool = False
