package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.convention.AInNameConvention;


import java.util.Objects;

/**
 * One checker rule and its configured reaction.
 *
 * @param convention expected naming convention
 * @param reaction reaction emitted when the convention is violated
 */
public record AIcdConventionCheckRule(
        AInNameConvention convention,
        AInConventionViolationReaction reaction) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdConventionCheckRule {
        Objects.requireNonNull(convention, "convention");
        Objects.requireNonNull(reaction, "reaction");
    }
}
