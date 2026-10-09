package eu.algites.lib.common.documentation.model;

/**
 * Represents a standalone code block.
 * <p>
 * The language identifier is renderer-neutral. Renderers may use it for syntax
 * highlighting, language labels, or output-specific metadata. An empty language
 * identifier means that the language is unspecified.
 */
public interface AIiDocumentationCodeBlock extends AIiDocumentationBlockElement {

    /**
     * Returns the language identifier of this code block.
     *
     * @return language identifier, for example {@code java}, {@code xml}, {@code text}, or an empty string if unspecified
     */
    String getLanguageId();

    /**
     * Returns the code block content.
     *
     * @return code text
     */
    String getCode();
}
