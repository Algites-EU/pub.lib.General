from __future__ import annotations

from eu.algites.lib.naming.convention.aic_algites_naming_profiles import AIcAlgitesNamingProfiles
from eu.algites.lib.naming.validation.aicd_convention_validation_profile import AIcdConventionValidationProfile
from eu.algites.lib.naming.validation.aicd_convention_validation_rule import AIcdConventionValidationRule
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction
from eu.algites.lib.naming.convention.ain_input_name_kind import AInInputNameKind

class AIcAlgitesConventionProfiles:
    """Provides strict Algites convention-validation profiles."""

    @staticmethod
    def strict() -> AIcdConventionValidationProfile:
        """Return the strict Algites convention-validation profile."""
        naming = AIcAlgitesNamingProfiles.java_profile()
        mapping = {
            AInConventionSubject.DEFINITION_NAME: AInInputNameKind.DEFINITION,
            AInConventionSubject.PROPERTY_NAME: AInInputNameKind.PROPERTY,
            AInConventionSubject.ENUM_VALUE: AInInputNameKind.ENUM_VALUE,
            AInConventionSubject.SYMBOLIC_MAP_KEY: AInInputNameKind.SYMBOLIC_MAP_KEY,
            AInConventionSubject.PACKAGE_SEGMENT: AInInputNameKind.PACKAGE_SEGMENT,
        }
        rules = {subject: AIcdConventionValidationRule(naming.input_conventions[kind], AInConventionViolationReaction.ERROR) for subject, kind in mapping.items()}
        return AIcdConventionValidationProfile(True, rules, naming.input_version_policy, naming.output_version_policy, AInConventionViolationReaction.ERROR, AInConventionViolationReaction.ERROR)
