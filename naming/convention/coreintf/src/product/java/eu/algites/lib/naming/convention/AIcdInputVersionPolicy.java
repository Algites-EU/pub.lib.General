package eu.algites.lib.naming.convention;

import java.util.Objects;

/**
 * Policy for extracting and validating a definition version independently from ordinary name tokens.
 *
 * @param source ordered version-source strategy
 * @param fileNameSeparator separator between logical name and filename version
 * @param requirePositiveInteger whether resolved versions must be positive integers
 * @param requireMatchingSources whether explicit and filename versions must agree when both are present
 */
public record AIcdInputVersionPolicy(
        AInVersionSource source,
        String fileNameSeparator,
        boolean requirePositiveInteger,
        boolean requireMatchingSources) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdInputVersionPolicy {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(fileNameSeparator, "fileNameSeparator");
    }
}
