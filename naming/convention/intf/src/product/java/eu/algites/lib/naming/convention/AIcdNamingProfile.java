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


    /** Returns a derived profile with a single input convention override. */
    public AIcdNamingProfile withInputConvention(AInInputNameKind aKind, AInNameConvention aConvention) {
        var locConventions = new java.util.EnumMap<AInInputNameKind, AInNameConvention>(AInInputNameKind.class);
        locConventions.putAll(inputConventions);
        locConventions.put(Objects.requireNonNull(aKind, "kind"), Objects.requireNonNull(aConvention, "convention"));
        return withInputConventions(locConventions);
    }

    /** Returns a derived profile with the entire input-convention map replaced. */
    public AIcdNamingProfile withInputConventions(Map<AInInputNameKind, AInNameConvention> aConventions) {
        return new AIcdNamingProfile(aConventions, outputRules, inputVersionPolicy, outputVersionPolicy);
    }

    /** Returns a derived profile with one output-name rule replaced. */
    public AIcdNamingProfile withOutputRule(AInOutputNameKind aKind, AIcdOutputNameRule aRule) {
        var locRules = new java.util.EnumMap<AInOutputNameKind, AIcdOutputNameRule>(AInOutputNameKind.class);
        locRules.putAll(outputRules);
        locRules.put(Objects.requireNonNull(aKind, "kind"), Objects.requireNonNull(aRule, "rule"));
        return withOutputRules(locRules);
    }

    /** Returns a derived profile with the entire output-rule map replaced. */
    public AIcdNamingProfile withOutputRules(Map<AInOutputNameKind, AIcdOutputNameRule> aRules) {
        return new AIcdNamingProfile(inputConventions, aRules, inputVersionPolicy, outputVersionPolicy);
    }

    /** Returns a derived profile with a different input-version policy. */
    public AIcdNamingProfile withInputVersionPolicy(AIcdInputVersionPolicy aPolicy) {
        return new AIcdNamingProfile(inputConventions, outputRules, aPolicy, outputVersionPolicy);
    }

    /** Returns a derived profile with a different output-version policy. */
    public AIcdNamingProfile withOutputVersionPolicy(AIcdOutputVersionPolicy aPolicy) {
        return new AIcdNamingProfile(inputConventions, outputRules, inputVersionPolicy, aPolicy);
    }
}
