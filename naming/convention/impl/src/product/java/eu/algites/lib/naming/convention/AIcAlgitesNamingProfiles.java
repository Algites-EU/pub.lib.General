package eu.algites.lib.naming.convention;


import java.util.EnumMap;
import java.util.Map;

/** Standard strict Algites naming profiles for generated Java and Python sources. */
public final class AIcAlgitesNamingProfiles {
    private AIcAlgitesNamingProfiles() {
    }

    /**
     * Creates the standard strict Algites naming profile for Java output.
     *
     * @return Java-oriented Algites naming profile
     */
    public static AIcdNamingProfile javaProfile() {
        return profile(AInNameConvention.LOWER_CAMEL_CASE);
    }

    /**
     * Creates the standard strict Algites naming profile for Python output.
     *
     * @return Python-oriented Algites naming profile
     */
    public static AIcdNamingProfile pythonProfile() {
        return profile(AInNameConvention.LOWER_SNAKE_CASE);
    }

    private static AIcdNamingProfile profile(AInNameConvention propertyConvention) {
        Map<AInInputNameKind, AInNameConvention> input = new EnumMap<>(AInInputNameKind.class);
        input.put(AInInputNameKind.DEFINITION, AInNameConvention.LOWER_KEBAB_CASE);
        input.put(AInInputNameKind.PROPERTY, AInNameConvention.UPPER_CAMEL_CASE);
        input.put(AInInputNameKind.ENUM_VALUE, AInNameConvention.LOWER_SNAKE_CASE);
        input.put(AInInputNameKind.SYMBOLIC_MAP_KEY, AInNameConvention.LOWER_SNAKE_CASE);
        input.put(AInInputNameKind.PACKAGE_SEGMENT, AInNameConvention.LOWER_SNAKE_CASE);
        input.put(AInInputNameKind.FILE_STEM, AInNameConvention.LOWER_KEBAB_CASE);

        Map<AInOutputNameKind, AIcdOutputNameRule> output = new EnumMap<>(AInOutputNameKind.class);
        output.put(AInOutputNameKind.DATA_TYPE, new AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "cgd", ""));
        output.put(AInOutputNameKind.ENUM_TYPE, new AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "ng", ""));
        output.put(AInOutputNameKind.INTERFACE_TYPE, new AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "ig", ""));
        output.put(AInOutputNameKind.PROPERTY, new AIcdOutputNameRule(propertyConvention, "", "", ""));
        output.put(AInOutputNameKind.ENUM_CONSTANT, new AIcdOutputNameRule(AInNameConvention.UPPER_SNAKE_CASE, "", "", ""));
        output.put(AInOutputNameKind.PACKAGE_SEGMENT, new AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "", "", ""));
        output.put(AInOutputNameKind.FILE_STEM, new AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "", "", ""));
        output.put(AInOutputNameKind.DATA_TYPE_FILE_STEM, new AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aicgd_", "", ""));
        output.put(AInOutputNameKind.ENUM_TYPE_FILE_STEM, new AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aing_", "", ""));
        output.put(AInOutputNameKind.INTERFACE_TYPE_FILE_STEM, new AIcdOutputNameRule(AInNameConvention.LOWER_SNAKE_CASE, "aiig_", "", ""));
        return new AIcdNamingProfile(
                input,
                output,
                new AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, "_", true, true),
                new AIcdOutputVersionPolicy(true, "_", "", ""));
    }
}
