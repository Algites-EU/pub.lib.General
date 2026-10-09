package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Represents a paragraph with ordered inline content.
 * <p>
 * A paragraph is a block element whose visible text is composed from inline
 * elements such as plain text, links, semantic references, line breaks, and
 * inline code. Renderers should preserve the order of inline elements and apply
 * output-specific escaping and formatting.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify inline content should use
 * {@link AIiDocumentationParagraphWriteAccess}.
 */
public interface AIiDocumentationParagraph extends AIiDocumentationBlockElement {

    /**
     * Returns paragraph inline elements in their rendering order.
     *
     * @return read-only ordered inline element list
     */
    List<AIiDocumentationInlineElement> getInlineElements();
}
