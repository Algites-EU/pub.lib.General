package eu.algites.lib.naming.validation;

import java.util.List;

/** Checks governed naming and version conventions without assuming a particular source format. */
public interface AIiConventionChecker {
    /**
     * Checks one name against the convention configured for its semantic subject.
     *
     * @param subject semantic kind of the name
     * @param value name to check
     * @param profile active convention-check profile
     * @return immutable list of convention violations
     */
    List<AIcdConventionViolation> checkName(AInConventionSubject subject, String value, AIcdConventionCheckProfile profile);

    /**
     * Checks extraction and consistency of an input canonical version.
     *
     * @param value source name potentially containing a version suffix
     * @param explicitVersion explicit metadata version, or {@code null}
     * @param profile active convention-check profile
     * @return immutable list of version violations
     */
    List<AIcdConventionViolation> checkInputVersion(String value, Integer explicitVersion, AIcdConventionCheckProfile profile);

    /**
     * Checks whether an already rendered output version matches the configured output-version policy.
     *
     * @param version canonical definition version
     * @param renderedSuffix rendered version suffix
     * @param profile active convention-check profile
     * @return immutable list of version-rendering violations
     */
    List<AIcdConventionViolation> checkOutputVersion(Integer version, String renderedSuffix, AIcdConventionCheckProfile profile);
}
