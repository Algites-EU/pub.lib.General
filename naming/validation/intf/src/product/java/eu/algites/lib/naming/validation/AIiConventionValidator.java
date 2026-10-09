package eu.algites.lib.naming.validation;

import java.util.List;

/**
 * Validates governed naming and version conventions without assuming a particular source format.
 *
 * <p>This interface is an extension point. Consumers may provide alternative implementations that
 * apply different convention semantics while retaining the common validation operation shape.</p>
 */
public interface AIiConventionValidator {
    /**
     * Validates one name against the convention configured for its semantic subject.
     *
     * @param subject semantic kind of the name
     * @param value name to check
     * @param profile active convention-validation profile
     * @return immutable list of convention violations
     */
    List<AIcdConventionViolation> validateName(AInConventionSubject subject, String value, AIcdConventionValidationProfile profile);

    /**
     * Validates extraction and consistency of an input canonical version.
     *
     * @param value source name potentially containing a version suffix
     * @param explicitVersion explicit metadata version, or {@code null}
     * @param profile active convention-validation profile
     * @return immutable list of version violations
     */
    List<AIcdConventionViolation> validateInputVersion(String value, Integer explicitVersion, AIcdConventionValidationProfile profile);

    /**
     * Validates whether an already rendered output version matches the configured output-version policy.
     *
     * @param version canonical definition version
     * @param renderedSuffix rendered version suffix
     * @param profile active convention-validation profile
     * @return immutable list of version-rendering violations
     */
    List<AIcdConventionViolation> validateOutputVersion(Integer version, String renderedSuffix, AIcdConventionValidationProfile profile);
}
