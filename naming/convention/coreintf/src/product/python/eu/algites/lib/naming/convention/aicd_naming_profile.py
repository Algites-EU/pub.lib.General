from __future__ import annotations

from dataclasses import dataclass
from typing import Mapping
from eu.algites.lib.naming.convention.aicd_input_version_policy import AIcdInputVersionPolicy
from eu.algites.lib.naming.convention.aicd_output_name_rule import AIcdOutputNameRule
from eu.algites.lib.naming.convention.aicd_output_version_policy import AIcdOutputVersionPolicy
from eu.algites.lib.naming.convention.ain_input_name_kind import AInInputNameKind
from eu.algites.lib.naming.convention.ain_name_convention import AInNameConvention
from eu.algites.lib.naming.convention.ain_output_name_kind import AInOutputNameKind

@dataclass(frozen=True, slots=True)
class AIcdNamingProfile:
    """Carries immutable naming profile data.

    Attributes:
        input_conventions: Source-subject naming conventions.
        output_rules: Target-subject rendering rules.
        input_version_policy: Input canonical-version policy.
        output_version_policy: Output canonical-version policy.
    """
    input_conventions: Mapping[AInInputNameKind, AInNameConvention]
    output_rules: Mapping[AInOutputNameKind, AIcdOutputNameRule]
    input_version_policy: AIcdInputVersionPolicy
    output_version_policy: AIcdOutputVersionPolicy
