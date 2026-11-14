package eu.algites.lib.text.format;

import eu.algites.lib.common.enums.AIiEnumItem;

/** Built-in text syntax categories with stable external codes.
 * @author linhart1
 */
public enum AInTextFormatType implements AIiEnumItem<String> {
    /** JSON document format. */ JSON("JSON"),
    /** YAML document format. */ YAML("YAML"),
    /** XML document format. */ XML("XML");

    private final String code;

    /** @param aCode persistent format code */
    AInTextFormatType(String aCode) { code = aCode; }

    /** {@inheritDoc} */
    @Override public String code() { return code; }
}
