from __future__ import annotations

from eu.algites.lib.naming.validation.aicd_convention_violation import AIcdConventionViolation
from eu.algites.lib.naming.conversion.aic_default_name_converter import AIcDefaultNameConverter
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction

class AIcDefaultConventionChecker:
    """Provides default convention checker functionality."""
    def __init__(self) -> None:
        """Initialize this service instance."""
        self._converter = AIcDefaultNameConverter()

    def check_name(self, subject, value, profile):
        """Check one name against its subject-specific naming convention."""
        if not profile.enabled:
            return ()
        rule = profile.naming_rules.get(subject)
        if rule is None or rule.reaction is AInConventionViolationReaction.IGNORE or self._converter.conforms(value, rule.convention):
            return ()
        return (AIcdConventionViolation(subject, rule.reaction, value, f"Expected {rule.convention.value} naming."),)

    def check_input_version(self, value, explicit_version, profile):
        """Check extraction and consistency of an input canonical version."""
        if not profile.enabled or profile.input_version_reaction is AInConventionViolationReaction.IGNORE:
            return ()
        try:
            self._converter.parse_versioned_name(value, explicit_version, profile.input_version_policy)
            return ()
        except ValueError as exc:
            return (AIcdConventionViolation(AInConventionSubject.INPUT_VERSION, profile.input_version_reaction, value, str(exc)),)

    def check_output_version(self, version, rendered_suffix, profile):
        """Check rendering of an output canonical version."""
        if not profile.enabled or profile.output_version_reaction is AInConventionViolationReaction.IGNORE:
            return ()
        expected = self._converter.render_version(version, profile.output_version_policy)
        if expected == rendered_suffix:
            return ()
        return (AIcdConventionViolation(AInConventionSubject.OUTPUT_VERSION, profile.output_version_reaction, rendered_suffix, f"Expected version rendering {expected!r}."),)
