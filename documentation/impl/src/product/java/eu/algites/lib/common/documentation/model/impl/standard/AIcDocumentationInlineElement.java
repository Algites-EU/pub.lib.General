package eu.algites.lib.common.documentation.model.impl.standard;


import eu.algites.lib.common.documentation.model.AIiDocumentationInlineElement;
import eu.algites.lib.common.documentation.model.AIiDocumentationInlineElementWriteAccess;
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
