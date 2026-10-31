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

    /**
     * Convert a previously validated identifier between named lexical conventions.
     *
     * @param aValue source identifier lexeme
     * @param aSource source convention ID
     * @param aTarget target convention ID
     * @return identifier with its segments converted using the loaded catalog
     * @throws IllegalArgumentException if a named convention is unknown or lacks a conversion style
     */
    default String convertIdentifierByDeclaredStyle(String aValue, String aSource, String aTarget) {
        throw new UnsupportedOperationException("Identifier style conversion is not supported by this catalog");
    }

    /**
     * Return the identifier convention configured for a structural name role.
     *
     * @param aProfile exact source naming profile
     * @param aStructure exact structure role, such as {@code type_name}
     * @return declared identifier convention, or {@code null} if none is present
     */
    default String identifierConvention(String aProfile, String aStructure) {
        throw new UnsupportedOperationException("No identifier convention API in this catalog");
    }

    /**
     * Returns the naming profile bound to a semantic DataObject generation role.
     *
     * @param aRole non-null semantic role identifier; must be defined by the caller's naming configuration
     * @return selected naming profile identifier, or {@code null} when the role is not bound
     */
    String selectedRole(String aRole);

    /**
     * Returns the selected generated-method naming profile.
     *
     * @return selected profile identifier, or {@code null} when none is selected
     */
    String generatedMethodProfile();

    /**
     * Resolves the structural composition reference configured for a generated method role.
     * This is a composition identifier, not the rendered Java method name. An absent
     * or blank mapping disables the optional accessor; callers must not silently
     * substitute a default method name.
     *
     * @param aRole generated-method role, for example {@code raw_getter},
     *              {@code raw_presence_checker_method}, or {@code raw_unsetter}
     * @return configured composition identifier, or {@code null} if there is no
     *         selected generated-method profile or the role has no mapping
     */
    String generatedMethodReference(String aRole);

    /** Checks a name against the complete structural naming pattern. */
    boolean matches(String aProfile, String aStructure, String aName);

}
