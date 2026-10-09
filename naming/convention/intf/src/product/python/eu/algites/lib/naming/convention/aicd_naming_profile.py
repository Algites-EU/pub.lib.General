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

    def with_input_convention(self, kind: AInInputNameKind, convention: AInNameConvention) -> AIcdNamingProfile:
        """Return a new profile with a single input-convention override."""
        updated = dict(self.input_conventions)
        updated[kind] = convention
        return self.with_input_conventions(updated)

    def with_input_conventions(self, conventions: Mapping[AInInputNameKind, AInNameConvention]) -> AIcdNamingProfile:
        """Replace input conventions without modifying the original profile."""
        return AIcdNamingProfile(dict(conventions), self.output_rules, self.input_version_policy, self.output_version_policy)

    def with_output_rule(self, kind: AInOutputNameKind, rule: AIcdOutputNameRule) -> AIcdNamingProfile:
        """Return a new profile with a single output-name rule override."""
        updated = dict(self.output_rules)
        updated[kind] = rule
        return self.with_output_rules(updated)

    def with_output_rules(self, rules: Mapping[AInOutputNameKind, AIcdOutputNameRule]) -> AIcdNamingProfile:
        """Replace output rules without modifying the original profile."""
        return AIcdNamingProfile(self.input_conventions, dict(rules), self.input_version_policy, self.output_version_policy)

    def with_input_version_policy(self, policy: AIcdInputVersionPolicy) -> AIcdNamingProfile:
        """Return a new profile with a different input-version policy."""
        return AIcdNamingProfile(self.input_conventions, self.output_rules, policy, self.output_version_policy)

    def with_output_version_policy(self, policy: AIcdOutputVersionPolicy) -> AIcdNamingProfile:
        """Return a new profile with a different output-version policy."""
        return AIcdNamingProfile(self.input_conventions, self.output_rules, self.input_version_policy, policy)

