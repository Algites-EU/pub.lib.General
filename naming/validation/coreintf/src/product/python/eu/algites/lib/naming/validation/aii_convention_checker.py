from __future__ import annotations

from typing import Protocol, Sequence

from eu.algites.lib.naming.validation.aicd_convention_check_profile import AIcdConventionCheckProfile
from eu.algites.lib.naming.validation.aicd_convention_violation import AIcdConventionViolation
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject


class AIiConventionChecker(Protocol):
    """Defines naming and canonical-version convention validation operations."""

    def check_name(self, subject: AInConventionSubject, value: str, profile: AIcdConventionCheckProfile) -> Sequence[AIcdConventionViolation]:
        """Check one name against its subject-specific naming convention."""
        ...

    def check_input_version(self, value: str, explicit_version: int | None, profile: AIcdConventionCheckProfile) -> Sequence[AIcdConventionViolation]:
        """Check extraction and consistency of an input canonical version."""
        ...

    def check_output_version(self, version: int | None, rendered_suffix: str, profile: AIcdConventionCheckProfile) -> Sequence[AIcdConventionViolation]:
        """Check rendering of an output canonical version."""
        ...
