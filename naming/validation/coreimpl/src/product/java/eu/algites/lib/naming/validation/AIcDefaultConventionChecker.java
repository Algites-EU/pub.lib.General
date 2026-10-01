package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.conversion.AIcDefaultNameConverter;


import java.util.List;

/** Default convention checker sharing the same naming/version semantics as code generation. */
public final class AIcDefaultConventionChecker implements AIiConventionChecker {
    /** Creates a convention checker backed by the default name converter. */
    public AIcDefaultConventionChecker() {
    }
    private final AIcDefaultNameConverter converter = new AIcDefaultNameConverter();

    /**
     * Checks one governed name using the configured subject-specific naming rule.
     *
     * @param subject semantic kind of the name
     * @param value name to check
     * @param profile active convention-check profile
     * @return immutable list of naming violations
     */
    @Override
    public List<AIcdConventionViolation> checkName(AInConventionSubject subject, String value, AIcdConventionCheckProfile profile) {
        if (!profile.enabled()) return List.of();
        AIcdConventionCheckRule rule = profile.namingRules().get(subject);
        if (rule == null || rule.reaction() == AInConventionViolationReaction.IGNORE || converter.conforms(value, rule.convention())) return List.of();
        return List.of(new AIcdConventionViolation(subject, rule.reaction(), value, "Expected " + rule.convention() + " naming."));
    }

    /**
     * Checks canonical version extraction and cross-source consistency for an input name.
     *
     * @param value source name potentially containing a version suffix
     * @param explicitVersion explicit canonical version, or {@code null}
     * @param profile active convention-check profile
     * @return immutable list of input-version violations
     */
    @Override
    public List<AIcdConventionViolation> checkInputVersion(String value, Integer explicitVersion, AIcdConventionCheckProfile profile) {
        if (!profile.enabled() || profile.inputVersionReaction() == AInConventionViolationReaction.IGNORE) return List.of();
        try {
            converter.parseVersionedName(value, explicitVersion, profile.inputVersionPolicy());
            return List.of();
        } catch (IllegalArgumentException ex) {
            return List.of(new AIcdConventionViolation(AInConventionSubject.INPUT_VERSION, profile.inputVersionReaction(), value, ex.getMessage()));
        }
    }

    /**
     * Checks a rendered canonical version suffix against the configured output policy.
     *
     * @param version canonical definition version
     * @param renderedSuffix rendered suffix to check
     * @param profile active convention-check profile
     * @return immutable list of output-version violations
     */
    @Override
    public List<AIcdConventionViolation> checkOutputVersion(Integer version, String renderedSuffix, AIcdConventionCheckProfile profile) {
        if (!profile.enabled() || profile.outputVersionReaction() == AInConventionViolationReaction.IGNORE) return List.of();
        String expected = converter.renderVersion(version, profile.outputVersionPolicy());
        if (expected.equals(renderedSuffix)) return List.of();
        return List.of(new AIcdConventionViolation(AInConventionSubject.OUTPUT_VERSION, profile.outputVersionReaction(), renderedSuffix, "Expected version rendering '" + expected + "'."));
    }
}
