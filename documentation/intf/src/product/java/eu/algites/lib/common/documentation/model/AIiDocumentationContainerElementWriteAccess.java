package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Provides write access to a block-level documentation container.
 * <p>
 * The read-only contract is defined by
 * {@link AIiDocumentationContainerElement}. This capability interface is
 * intended for construction, aggregation, and model-processing phases.
 * Validators, renderers, and other read-only consumers should depend on the
 * read-only interface instead.
 */
public interface AIiDocumentationContainerElementWriteAccess
        extends AIiDocumentationContainerElement,
        AIiDocumentationBlockElementWriteAccess {

    /**
     * Adds a child block element at the end of the container.
     *
     * @param aChildElement child block element to add
     */
    void addChildElement(AIiDocumentationBlockElement aChildElement);

    /**
     * Adds multiple child block elements in the provided order.
     *
     * @param aChildElements child block elements to add
     */
    void addChildElements(List<? extends AIiDocumentationBlockElement> aChildElements);
}
