package eu.algites.lib.naming.convention;

import java.util.Objects;

/**
 * Rendering rule for one output-name subject.
 *
 * @param convention target naming convention
 * @param prefix literal prefix added before the normalized name
 * @param typeMarker semantic type marker inserted after the prefix
 * @param suffix literal suffix added before any separately rendered version
 */
public record AIcdOutputNameRule(
        AInNameConvention convention,
        String prefix,
        String typeMarker,
        String suffix) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdOutputNameRule {
        Objects.requireNonNull(convention, "convention");
        Objects.requireNonNull(prefix, "prefix");
        Objects.requireNonNull(typeMarker, "typeMarker");
        Objects.requireNonNull(suffix, "suffix");
    }
}
