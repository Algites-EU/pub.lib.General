package eu.algites.lib.naming.validation;

import eu.algites.lib.naming.convention.AIcAlgitesNamingProfiles;
import eu.algites.lib.naming.convention.AIcdNamingProfile;
import eu.algites.lib.naming.convention.AInInputNameKind;


import java.util.EnumMap;
import java.util.Map;

/** Strict Algites convention-check profiles. */
public final class AIcAlgitesConventionProfiles {
    private AIcAlgitesConventionProfiles() {
    }

    /**
     * Creates the strict Algites convention-check profile with all governed naming and version checks enabled as errors.
     *
     * @return strict Algites convention-check profile
     */
    public static AIcdConventionCheckProfile strict() {
        AIcdNamingProfile naming = AIcAlgitesNamingProfiles.javaProfile();
        Map<AInConventionSubject, AIcdConventionCheckRule> rules = new EnumMap<>(AInConventionSubject.class);
        rules.put(AInConventionSubject.DEFINITION_NAME, rule(naming, AInInputNameKind.DEFINITION));
        rules.put(AInConventionSubject.PROPERTY_NAME, rule(naming, AInInputNameKind.PROPERTY));
        rules.put(AInConventionSubject.ENUM_VALUE, rule(naming, AInInputNameKind.ENUM_VALUE));
        rules.put(AInConventionSubject.SYMBOLIC_MAP_KEY, rule(naming, AInInputNameKind.SYMBOLIC_MAP_KEY));
        rules.put(AInConventionSubject.PACKAGE_SEGMENT, rule(naming, AInInputNameKind.PACKAGE_SEGMENT));
        return new AIcdConventionCheckProfile(
                true,
                rules,
                naming.inputVersionPolicy(),
                naming.outputVersionPolicy(),
                AInConventionViolationReaction.ERROR,
                AInConventionViolationReaction.ERROR);
    }

    private static AIcdConventionCheckRule rule(AIcdNamingProfile profile, AInInputNameKind kind) {
        return new AIcdConventionCheckRule(profile.inputConventions().get(kind), AInConventionViolationReaction.ERROR);
    }
}
