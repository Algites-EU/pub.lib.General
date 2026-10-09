package eu.algites.lib.common.documentation.model;

/**
 * Default base implementation of {@link AIiDocumentationBlockElementWriteAccess}.
 * <p>
 * See {@link AIiDocumentationBlockElement} for the read-only block element contract.
 */
public abstract class AIcDocumentationBlockElement
        extends AIcDocumentationElement
        implements AIiDocumentationBlockElementWriteAccess {

    /**
     * Creates a block-level documentation element.
     *
     * @param aElementId stable element identifier
     */
    protected AIcDocumentationBlockElement(String aElementId) {
        super(aElementId);
    }
}
