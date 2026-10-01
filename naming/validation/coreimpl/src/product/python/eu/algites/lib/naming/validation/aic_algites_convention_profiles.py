from __future__ import annotations

from eu.algites.lib.naming.convention.aic_algites_naming_profiles import AIcAlgitesNamingProfiles
from eu.algites.lib.naming.validation.aicd_convention_check_profile import AIcdConventionCheckProfile
from eu.algites.lib.naming.validation.aicd_convention_check_rule import AIcdConventionCheckRule
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction
from eu.algites.lib.naming.convention.ain_input_name_kind import AInInputNameKind

class AIcAlgitesConventionProfiles:
    """Provides strict Algites convention-check profiles."""

    @staticmethod
    def strict() -> AIcdConventionCheckProfile:
        """Return the strict Algites convention-check profile."""
        naming = AIcAlgitesNamingProfiles.java_profile()
        mapping = {
            AInConventionSubject.DEFINITION_NAME: AInInputNameKind.DEFINITION,
            AInConventionSubject.PROPERTY_NAME: AInInputNameKind.PROPERTY,
            AInConventionSubject.ENUM_VALUE: AInInputNameKind.ENUM_VALUE,
            AInConventionSubject.SYMBOLIC_MAP_KEY: AInInputNameKind.SYMBOLIC_MAP_KEY,
            AInConventionSubject.PACKAGE_SEGMENT: AInInputNameKind.PACKAGE_SEGMENT,
        }
        rules = {subject: AIcdConventionCheckRule(naming.input_conventions[kind], AInConventionViolationReaction.ERROR) for subject, kind in mapping.items()}
        return AIcdConventionCheckProfile(True, rules, naming.input_version_policy, naming.output_version_policy, AInConventionViolationReaction.ERROR, AInConventionViolationReaction.ERROR)
