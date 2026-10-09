package eu.algites.lib.common.documentation.model;

/**
 * Default base implementation of {@link AIiDocumentationInlineElementWriteAccess}.
 * <p>
 * See {@link AIiDocumentationInlineElement} for the read-only inline element contract.
 */
public abstract class AIcDocumentationInlineElement
        extends AIcDocumentationElement
        implements AIiDocumentationInlineElementWriteAccess {

    /**
     * Creates an inline documentation element.
     *
     * @param aElementId stable element identifier
     */
    protected AIcDocumentationInlineElement(String aElementId) {
        super(aElementId);
    }
}
