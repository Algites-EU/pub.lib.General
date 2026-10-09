package eu.algites.lib.common.documentation.model;

/**
 * Represents plain inline text.
 * <p>
 * The text is renderer-neutral plain text. Renderers are responsible for
 * escaping it for their output format. Paragraph-level structure should be
 * represented by paragraph elements; forced line breaks inside a paragraph
 * should be represented by {@link AIiDocumentationLineBreak}.
 */
public interface AIiDocumentationText extends AIiDocumentationInlineElement {

    /**
     * Returns the plain text content.
     *
     * @return plain text content
     */
    String getText();
}
