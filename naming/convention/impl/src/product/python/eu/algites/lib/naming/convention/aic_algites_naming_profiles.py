from __future__ import annotations

from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_naming_profile import AIcdNamingProfile
from eu.algites.lib.naming.convention.aicd_output_name_rule import AIcdOutputNameRule
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.convention.ain_input_name_kind import AInInputNameKind
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention
from eu.algites.lib.naming.convention.ain_output_name_kind import AInOutputNameKind
from eu.algites.lib.naming.convention.ain_version_source import AInVersionSource

def _profile(property_convention: AInNameConvention) -> AIcdNamingProfile:
    """Build the shared Algites naming profile for a target property convention."""
    inputs = {
        AInInputNameKind.DEFINITION: AInNameConvention.LOWER_KEBAB_CASE,
        AInInputNameKind.PROPERTY: AInNameConvention.UPPER_CAMEL_CASE,
        AInInputNameKind.ENUM_VALUE: AInNameConvention.LOWER_SNAKE_CASE,
        AInInputNameKind.SYMBOLIC_MAP_KEY: AInNameConvention.LOWER_SNAKE_CASE,
        AInInputNameKind.PACKAGE_SEGMENT: AInNameConvention.LOWER_SNAKE_CASE,
        AInInputNameKind.FILE_STEM: AInNameConvention.LOWER_KEBAB_CASE,
    }
    outputs = {
        AInOutputNameKind.DATA_TYPE: AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "cgd"),
        AInOutputNameKind.ENUM_TYPE: AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "ng"),
        AInOutputNameKind.INTERFACE_TYPE: AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "ig"),
        AInOutputNameKind.PROPERTY: AIcdOutputNameRule(property_convention),
        AInOutputNameKind.ENUM_CONSTANT: AIcdOutputNameRule(AInNameConvention.UPPER_SNAKE_CASE),
        AInOutputNameKind.PACKAGE_SEGMENT: AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE),
        AInOutputNameKind.FILE_STEM: AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE),
        AInOutputNameKind.DATA_TYPE_FILE_STEM: AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aicgd_"),
        AInOutputNameKind.ENUM_TYPE_FILE_STEM: AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aing_"),
        AInOutputNameKind.INTERFACE_TYPE_FILE_STEM: AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aiig_"),
    }
    return AIcdNamingProfile(
        inputs,
        outputs,
        AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, "_", True, True),
        AIcdOutputVersionPolicy(True, "_"),
    )


class AIcAlgitesNamingProfiles:
    """Provides standard strict Algites naming profiles for generated source."""

    @staticmethod
    def java_profile() -> AIcdNamingProfile:
        """Return the standard Algites naming profile for Java output."""
        return _profile(AInNameConvention.LOWER_CAMEL_CASE)

    @staticmethod
    def python_profile() -> AIcdNamingProfile:
        """Return the standard Algites naming profile for Python output."""
        return _profile(AInNameConvention.LOWER_SNAKE_CASE)
