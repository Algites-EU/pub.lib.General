package eu.algites.lib.naming.validation;


import java.util.Objects;

/**
 * Structured convention-check finding.
 *
 * @param subject semantic subject that violated a convention
 * @param reaction configured violation reaction
 * @param value offending source or rendered value
 * @param message human-readable diagnostic message
 */
public record AIcdConventionViolation(
        AInConventionSubject subject,
        AInConventionViolationReaction reaction,
        String value,
        String message) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdConventionViolation {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(message, "message");
    }
}
