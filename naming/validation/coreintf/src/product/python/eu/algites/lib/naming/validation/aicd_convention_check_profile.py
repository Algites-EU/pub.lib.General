from __future__ import annotations

from dataclasses import dataclass
from typing import Mapping
from eu.algites.lib.naming.validation.aicd_convention_check_rule import AIcdConventionCheckRule
from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.validation.ain_convention_subject import AInConventionSubject
from eu.algites.lib.naming.validation.ain_convention_violation_reaction import AInConventionViolationReaction

@dataclass(frozen=True, slots=True)
class AIcdConventionCheckProfile:
    """Carries immutable convention check profile data.

    Attributes:
        enabled: Whether checking is enabled.
        naming_rules: Subject-specific naming rules.
        input_version_policy: Input canonical-version policy.
        output_version_policy: Output canonical-version policy.
        input_version_reaction: Reaction to input-version violations.
        output_version_reaction: Reaction to output-version violations.
    """
    enabled: bool
    naming_rules: Mapping[AInConventionSubject, AIcdConventionCheckRule]
    input_version_policy: AIcdInputVersionPolicy
    output_version_policy: AIcdOutputVersionPolicy
    input_version_reaction: AInConventionViolationReaction
    output_version_reaction: AInConventionViolationReaction
