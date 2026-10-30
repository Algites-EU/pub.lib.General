package eu.algites.lib.naming.convention;

import java.util.Map;
import java.util.Set;

/**
 * Independent API for declarative, YAML-backed named compositions.
 *
 * @author linhart1
 */
public interface AIiNameConventionCatalog {
    /** Recognized convention IDs. */
    Set<String> names();

    /** Renders a structural name with captured variables and an optional source lexical convention. */
    String format(String aProfile, String aStructure, Map<String, String> aValues, String aSourceIdentifierConvention);

    /** Parses a structural name according to its composition. */
    Map<String, String> parse(String aProfile, String aStructure, String aName);

    /**
     * Resolves a configurable schema metadata property name for a source profile.
     * Empty means that the particular metadata field is deliberately not mapped.
     * Implementations that do not support schema-source metadata can retain this default;
     * a configured data-source writer must not infer metadata that lacks a source mapping.
     *
     * @param aProfile source-structure naming profile identifier
     * @param aSide {@code schema_source} (schema definition) or {@code data_source} (instance document)
     * @param aKind {@code url}, {@code identifier}, or {@code version}
     * @return configured property name or empty string if not provided
     */
    default String schemaMetadataField(String aProfile, String aSide, String aKind) {
        return "";
    }

    /**
     * Resolves a literal generated method name from a selected method naming profile.
     * Unlike a structural composition identifier, the result is the actual name of
     * the generated accessor. This includes the synthetic map entry getters.
     *
     * @param role non-null generated-method role identifier
     * @return literal method name, or an empty string if no role is selected
     * @throws IllegalArgumentException if the role identifier is invalid
     */
    default String generatedMethodLiteral(String role) {
        return "";
    }

    /** Checks a name against the complete structural naming pattern. */
    boolean matches(String aProfile, String aStructure, String aName);

}
