package eu.algites.lib.naming.convention;

import java.util.Map;
import java.util.Set;

/** Independent API for declarative, YAML-backed named compositions. */
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

    /** Checks a name against the complete structural naming pattern. */
    boolean matches(String aProfile, String aStructure, String aName);

}
