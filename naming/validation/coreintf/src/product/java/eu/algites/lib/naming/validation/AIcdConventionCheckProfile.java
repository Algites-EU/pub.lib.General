package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.convention.AIcdInputVersionPolicy;
import eu.algites.lib.naming.convention.AIcdOutputVersionPolicy;


import java.util.Map;
import java.util.Objects;

/**
 * Configurable convention-check profile.
 *
 * @param enabled whether this profile performs checks
 * @param namingRules subject-specific naming rules
 * @param inputVersionPolicy input canonical-version policy
 * @param outputVersionPolicy output canonical-version policy
 * @param inputVersionReaction reaction to input-version violations
 * @param outputVersionReaction reaction to output-version violations
 */
public record AIcdConventionCheckProfile(
        boolean enabled,
        Map<AInConventionSubject, AIcdConventionCheckRule> namingRules,
        AIcdInputVersionPolicy inputVersionPolicy,
        AIcdOutputVersionPolicy outputVersionPolicy,
        AInConventionViolationReaction inputVersionReaction,
        AInConventionViolationReaction outputVersionReaction) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdConventionCheckProfile {
        namingRules = Map.copyOf(Objects.requireNonNull(namingRules, "namingRules"));
        Objects.requireNonNull(inputVersionPolicy, "inputVersionPolicy");
        Objects.requireNonNull(outputVersionPolicy, "outputVersionPolicy");
        Objects.requireNonNull(inputVersionReaction, "inputVersionReaction");
        Objects.requireNonNull(outputVersionReaction, "outputVersionReaction");
    }
}
