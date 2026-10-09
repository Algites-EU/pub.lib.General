package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationTextWriteAccess}.
 * <p>
 * See {@link AIiDocumentationText} for the read-only text contract.
 */
public class AIcDocumentationText
        extends AIcDocumentationInlineElement
        implements AIiDocumentationTextWriteAccess {

    private final String text;

    /**
     * Creates a plain inline text element.
     *
     * @param aElementId stable element identifier
     * @param aText plain text content
     */
    public AIcDocumentationText(
            String aElementId,
            String aText) {
        super(aElementId);
        text = Objects.requireNonNull(aText, "Parameter aText must not be null.");
    }

    @Override
    public String getText() {
        return text;
    }
}
