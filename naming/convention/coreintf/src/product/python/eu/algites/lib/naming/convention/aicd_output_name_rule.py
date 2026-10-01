from __future__ import annotations

from dataclasses import dataclass
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention

@dataclass(frozen=True, slots=True)
class AIcdOutputNameRule:
    """Carries immutable output name rule data.

    Attributes:
        convention: Naming convention.
        prefix: Literal name prefix.
        type_marker: Semantic type marker.
        suffix: Literal name suffix.
    """
    convention: AInNameConvention
    prefix: str = ""
    type_marker: str = ""
    suffix: str = ""
