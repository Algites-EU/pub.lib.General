package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationInlineCodeWriteAccess}.
 * <p>
 * See {@link AIiDocumentationInlineCode} for the read-only inline code contract.
 */
public class AIcDocumentationInlineCode
        extends AIcDocumentationInlineElement
        implements AIiDocumentationInlineCodeWriteAccess {

    private final String code;

    /**
     * Creates an inline code element.
     *
     * @param aElementId stable element identifier
     * @param aCode inline code text
     */
    public AIcDocumentationInlineCode(
            String aElementId,
            String aCode) {
        super(aElementId);
        code = Objects.requireNonNull(aCode, "Parameter aCode must not be null.");
    }

    @Override
    public String getCode() {
        return code;
    }
}
