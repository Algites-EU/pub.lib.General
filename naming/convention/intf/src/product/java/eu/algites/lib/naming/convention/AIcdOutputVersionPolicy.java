package eu.algites.lib.naming.convention;

import java.util.Objects;

/**
 * Policy for rendering a canonical definition version in generated names.
 *
 * @param includeVersion whether the canonical version is rendered
 * @param separator separator placed before the version payload
 * @param prefix literal prefix inside the version payload
 * @param suffix literal suffix after the version payload
 */
public record AIcdOutputVersionPolicy(
        boolean includeVersion,
        String separator,
        String prefix,
        String suffix) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdOutputVersionPolicy {
        Objects.requireNonNull(separator, "separator");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(suffix, "suffix");
    }
}
