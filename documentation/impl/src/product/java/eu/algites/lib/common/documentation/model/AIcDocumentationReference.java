package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationReferenceWriteAccess}.
 * <p>
 * See {@link AIiDocumentationReference} for the read-only reference contract.
 */
public class AIcDocumentationReference
        extends AIcDocumentationInlineElement
        implements AIiDocumentationReferenceWriteAccess {

    private final String label;

    private final AIiDocumentationReferenceTarget referenceTarget;

    private final String title;

    /**
     * Creates a semantic inline reference.
     *
     * @param aElementId stable element identifier
     * @param aLabel human-readable reference label, or an empty string if it should be derived
     * @param aReferenceTarget structured semantic reference target
     * @param aTitle human-readable reference title, or an empty string if unavailable
     */
    public AIcDocumentationReference(
            String aElementId,
            String aLabel,
            AIiDocumentationReferenceTarget aReferenceTarget,
            String aTitle) {
        super(aElementId);
        label = Objects.requireNonNull(aLabel, "Parameter aLabel must not be null.");
        referenceTarget = Objects.requireNonNull(aReferenceTarget, "Parameter aReferenceTarget must not be null.");
        title = Objects.requireNonNull(aTitle, "Parameter aTitle must not be null.");
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public AIiDocumentationReferenceTarget getReferenceTarget() {
        return referenceTarget;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
