from __future__ import annotations

from dataclasses import dataclass
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction

@dataclass(frozen=True, slots=True)
class AIcdConventionViolation:
    """Carries immutable convention violation data.

    Attributes:
        subject: Convention subject.
        reaction: Convention-violation reaction.
        value: Offending value.
        message: Human-readable diagnostic.
    """
    subject: AInConventionSubject
    reaction: AInConventionViolationReaction
    value: str
    message: str
