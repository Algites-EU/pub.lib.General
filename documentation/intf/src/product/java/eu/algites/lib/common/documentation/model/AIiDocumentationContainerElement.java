package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Represents a block-level documentation element that can contain child blocks.
 * <p>
 * Containers are used for document roots, sections, and any future structural
 * block elements that need to aggregate other block elements. Inline elements
 * such as links, text, and references belong inside paragraph-like elements, not
 * directly inside containers.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify the child list should use
 * {@link AIiDocumentationContainerElementWriteAccess}.
 */
public interface AIiDocumentationContainerElement extends AIiDocumentationBlockElement {

    /**
     * Returns child block elements in their rendering order.
     *
     * @return read-only ordered child block element list
     */
    List<AIiDocumentationBlockElement> getChildElements();
}
