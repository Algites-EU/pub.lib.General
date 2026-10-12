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

    /** Checks a name against the complete structural naming pattern. */
    boolean matches(String aProfile, String aStructure, String aName);

}
