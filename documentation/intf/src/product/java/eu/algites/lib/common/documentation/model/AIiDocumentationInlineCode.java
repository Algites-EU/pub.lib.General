package eu.algites.lib.common.documentation.model;

/**
 * Represents inline code inside textual content.
 * <p>
 * Inline code is intended for short identifiers, keywords, literals, or code
 * fragments that should stay within a paragraph rather than become a standalone
 * code block.
 */
public interface AIiDocumentationInlineCode extends AIiDocumentationInlineElement {

    /**
     * Returns the inline code text.
     *
     * @return inline code text
     */
    String getCode();
}
