package eu.algites.lib.common.documentation.model;

/**
 * Default implementation of {@link AIiDocumentationLineBreakWriteAccess}.
 * <p>
 * See {@link AIiDocumentationLineBreak} for the read-only line break contract.
 */
public class AIcDocumentationLineBreak
        extends AIcDocumentationInlineElement
        implements AIiDocumentationLineBreakWriteAccess {

    /**
     * Creates an explicit inline line break.
     *
     * @param aElementId stable element identifier
     */
    public AIcDocumentationLineBreak(String aElementId) {
        super(aElementId);
    }
}
