from __future__ import annotations

from typing import Protocol, Sequence

from eu.algites.lib.naming.validation.aicd_convention_validation_profile import AIcdConventionValidationProfile
from eu.algites.lib.naming.validation.aicd_convention_violation import AIcdConventionViolation
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject


class AIiConventionValidator(Protocol):
    """Define naming and canonical-version validation operations.

    Alternative implementations may apply different convention semantics while
    retaining the common validation operation shape.
    """

    def validate_name(self, subject: AInConventionSubject, value: str, profile: AIcdConventionValidationProfile) -> Sequence[AIcdConventionViolation]:
        """Validate one name against its subject-specific naming convention."""
        ...

    def validate_input_version(self, value: str, explicit_version: int | None, profile: AIcdConventionValidationProfile) -> Sequence[AIcdConventionViolation]:
        """Validate extraction and consistency of an input canonical version."""
        ...

    def validate_output_version(self, version: int | None, rendered_suffix: str, profile: AIcdConventionValidationProfile) -> Sequence[AIcdConventionViolation]:
        """Validate rendering of an output canonical version."""
        ...
