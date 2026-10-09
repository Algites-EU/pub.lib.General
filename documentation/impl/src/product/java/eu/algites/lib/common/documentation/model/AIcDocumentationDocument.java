package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationDocumentWriteAccess}.
 * <p>
 * See {@link AIiDocumentationDocument} for the read-only document contract.
 */
public class AIcDocumentationDocument
        extends AIcDocumentationContainerElement
        implements AIiDocumentationDocumentWriteAccess {

    private final String documentTitle;

    private final String documentDescription;

    /**
     * Creates a documentation document.
     *
     * @param aElementId stable element identifier
     * @param aDocumentTitle human-readable document title
     * @param aDocumentDescription document description, or an empty string if unavailable
     */
    public AIcDocumentationDocument(
            String aElementId,
            String aDocumentTitle,
            String aDocumentDescription) {
        super(aElementId);
        documentTitle = Objects.requireNonNull(aDocumentTitle, "Parameter aDocumentTitle must not be null.");
        documentDescription = Objects.requireNonNull(aDocumentDescription, "Parameter aDocumentDescription must not be null.");
    }

    @Override
    public String getDocumentTitle() {
        return documentTitle;
    }

    @Override
    public String getDocumentDescription() {
        return documentDescription;
    }
}
