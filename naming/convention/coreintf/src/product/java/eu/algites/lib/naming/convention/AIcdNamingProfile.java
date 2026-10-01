package eu.algites.lib.naming.convention;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable input/output naming and version policy set.
 *
 * @param inputConventions expected conventions for source-name subjects
 * @param outputRules rendering rules for target-name subjects
 * @param inputVersionPolicy canonical input-version extraction policy
 * @param outputVersionPolicy canonical output-version rendering policy
 */
public record AIcdNamingProfile(
        Map<AInInputNameKind, AInNameConvention> inputConventions,
        Map<AInOutputNameKind, AIcdOutputNameRule> outputRules,
        AIcdInputVersionPolicy inputVersionPolicy,
        AIcdOutputVersionPolicy outputVersionPolicy) {
    /** Validates and normalizes the supplied data-object components. */
    public AIcdNamingProfile {
        inputConventions = Map.copyOf(Objects.requireNonNull(inputConventions, "inputConventions"));
        outputRules = Map.copyOf(Objects.requireNonNull(outputRules, "outputRules"));
        Objects.requireNonNull(inputVersionPolicy, "inputVersionPolicy");
        Objects.requireNonNull(outputVersionPolicy, "outputVersionPolicy");
    }
}
