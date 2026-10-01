from __future__ import annotations

from dataclasses import dataclass
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention

@dataclass(frozen=True, slots=True)
class AIcdConventionValidationRule:
    """Carries immutable convention-validation rule data.

    Attributes:
        convention: Naming convention.
        reaction: Convention-violation reaction.
    """
    convention: AInNameConvention
    reaction: AInConventionViolationReaction
