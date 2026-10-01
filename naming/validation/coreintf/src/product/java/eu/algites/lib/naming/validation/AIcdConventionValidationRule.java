package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.convention.AInNameConvention;


import java.util.Objects;

/**
 * One validation rule and its configured reaction.
 *
 * @param convention expected naming convention
 * @param reaction reaction emitted when the convention is violated
 */
public record AIcdConventionValidationRule(
        AInNameConvention convention,
        AInConventionViolationReaction reaction) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdConventionValidationRule {
        Objects.requireNonNull(convention, "convention");
        Objects.requireNonNull(reaction, "reaction");
    }
}
