package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.convention.AIcAlgitesNamingProfiles;
import eu.algites.lib.naming.convention.AIcdNamingProfile;
import eu.algites.lib.naming.convention.AInInputNameKind;


import java.util.EnumMap;
import java.util.Map;

/** Strict Algites convention-validation profiles. */
public final class AIcAlgitesConventionProfiles {
    private AIcAlgitesConventionProfiles() {
    }

    /**
     * Creates the strict Algites convention-validation profile with all governed naming and version validation enabled as errors.
     *
     * @return strict Algites convention-validation profile
     */
    public static AIcdConventionValidationProfile strict() {
        AIcdNamingProfile naming = AIcAlgitesNamingProfiles.javaProfile();
        Map<AInConventionSubject, AIcdConventionValidationRule> rules = new EnumMap<>(AInConventionSubject.class);
        rules.put(AInConventionSubject.DEFINITION_NAME, rule(naming, AInInputNameKind.DEFINITION));
        rules.put(AInConventionSubject.PROPERTY_NAME, rule(naming, AInInputNameKind.PROPERTY));
        rules.put(AInConventionSubject.ENUM_VALUE, rule(naming, AInInputNameKind.ENUM_VALUE));
        rules.put(AInConventionSubject.SYMBOLIC_MAP_KEY, rule(naming, AInInputNameKind.SYMBOLIC_MAP_KEY));
        rules.put(AInConventionSubject.PACKAGE_SEGMENT, rule(naming, AInInputNameKind.PACKAGE_SEGMENT));
        return new AIcdConventionValidationProfile(
                true,
                rules,
                naming.inputVersionPolicy(),
                naming.outputVersionPolicy(),
                AInConventionViolationReaction.ERROR,
                AInConventionViolationReaction.ERROR);
    }

    private static AIcdConventionValidationRule rule(AIcdNamingProfile profile, AInInputNameKind kind) {
        return new AIcdConventionValidationRule(profile.inputConventions().get(kind), AInConventionViolationReaction.ERROR);
    }
}
