package eu.algites.lib.naming.conversion;

import eu.algites.lib.naming.convention.AIcdInputVersionPolicy;
import eu.algites.lib.naming.convention.AIcdOutputVersionPolicy;
import eu.algites.lib.naming.convention.AInNameConvention;

import java.util.List;

/** Converts governed names through normalized word tokens. */
public interface AIiNameConverter {
    /**
     * Splits a source name into normalized lower-case word tokens according to its declared convention.
     *
     * @param value source name
     * @param convention declared source convention
     * @return immutable normalized word-token list
     */
    List<String> tokenize(String value, AInNameConvention convention);

    /**
     * Renders normalized word tokens using the requested target convention.
     *
     * @param tokens normalized word tokens
     * @param convention requested target convention
     * @return rendered name
     */
    String render(List<String> tokens, AInNameConvention convention);

    /**
     * Converts one name between explicitly declared naming conventions.
     *
     * @param value source name
     * @param inputConvention source convention
     * @param outputConvention target convention
     * @return converted name
     */
    String convert(String value, AInNameConvention inputConvention, AInNameConvention outputConvention);

    /**
     * Tests whether a name conforms exactly to the requested convention.
     *
     * @param value name to test
     * @param convention expected convention
     * @return {@code true} when the name conforms
     */
    boolean conforms(String value, AInNameConvention convention);

    /**
     * Separates a canonical logical name from its definition version according to the input-version policy.
     *
     * @param value source name, potentially carrying a governed version suffix
     * @param explicitVersion explicit metadata version, or {@code null}
     * @param policy input-version extraction and consistency policy
     * @return parsed logical name and separately tracked canonical version
     */
    AIcdParsedVersionedName parseVersionedName(String value, Integer explicitVersion, AIcdInputVersionPolicy policy);

    /**
     * Renders a canonical definition version independently from ordinary name conversion.
     *
     * @param version canonical definition version
     * @param policy output-version rendering policy
     * @return rendered version suffix, possibly empty when disabled
     */
    String renderVersion(Integer version, AIcdOutputVersionPolicy policy);
}
