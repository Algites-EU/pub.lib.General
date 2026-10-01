package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.conversion.AIcDefaultNameConverter;


import java.util.List;

/** Default convention validator sharing the same naming/version semantics as code generation. */
public final class AIcDefaultConventionValidator implements AIiConventionValidator {
    /** Creates a convention validator backed by the default name converter. */
    public AIcDefaultConventionValidator() {
    }
    private final AIcDefaultNameConverter converter = new AIcDefaultNameConverter();

    /**
     * Validates one governed name using the configured subject-specific naming rule.
     *
     * @param subject semantic kind of the name
     * @param value name to check
     * @param profile active convention-validation profile
     * @return immutable list of naming violations
     */
    @Override
    public List<AIcdConventionViolation> validateName(AInConventionSubject subject, String value, AIcdConventionValidationProfile profile) {
        if (!profile.enabled()) return List.of();
        AIcdConventionValidationRule rule = profile.namingRules().get(subject);
        if (rule == null || rule.reaction() == AInConventionViolationReaction.IGNORE || converter.conforms(value, rule.convention())) return List.of();
        return List.of(new AIcdConventionViolation(subject, rule.reaction(), value, "Expected " + rule.convention() + " naming."));
    }

    /**
     * Validates canonical version extraction and cross-source consistency for an input name.
     *
     * @param value source name potentially containing a version suffix
     * @param explicitVersion explicit canonical version, or {@code null}
     * @param profile active convention-validation profile
     * @return immutable list of input-version violations
     */
    @Override
    public List<AIcdConventionViolation> validateInputVersion(String value, Integer explicitVersion, AIcdConventionValidationProfile profile) {
        if (!profile.enabled() || profile.inputVersionReaction() == AInConventionViolationReaction.IGNORE) return List.of();
        try {
            converter.parseVersionedName(value, explicitVersion, profile.inputVersionPolicy());
            return List.of();
        } catch (IllegalArgumentException ex) {
            return List.of(new AIcdConventionViolation(AInConventionSubject.INPUT_VERSION, profile.inputVersionReaction(), value, ex.getMessage()));
        }
    }

    /**
     * Validates a rendered canonical version suffix against the configured output policy.
     *
     * @param version canonical definition version
     * @param renderedSuffix rendered suffix to check
     * @param profile active convention-validation profile
     * @return immutable list of output-version violations
     */
    @Override
    public List<AIcdConventionViolation> validateOutputVersion(Integer version, String renderedSuffix, AIcdConventionValidationProfile profile) {
        if (!profile.enabled() || profile.outputVersionReaction() == AInConventionViolationReaction.IGNORE) return List.of();
        String expected = converter.renderVersion(version, profile.outputVersionPolicy());
        if (expected.equals(renderedSuffix)) return List.of();
        return List.of(new AIcdConventionViolation(AInConventionSubject.OUTPUT_VERSION, profile.outputVersionReaction(), renderedSuffix, "Expected version rendering '" + expected + "'."));
    }
}
