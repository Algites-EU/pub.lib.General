package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationLinkWriteAccess}.
 * <p>
 * See {@link AIiDocumentationLink} for the read-only link contract.
 */
public class AIcDocumentationLink
        extends AIcDocumentationInlineElement
        implements AIiDocumentationLinkWriteAccess {

    private final String label;

    private final String target;

    private final String title;

    /**
     * Creates a non-semantic inline link.
     *
     * @param aElementId stable element identifier
     * @param aLabel human-readable link label
     * @param aTarget renderer-neutral link target
     * @param aTitle human-readable link title, or an empty string if unavailable
     */
    public AIcDocumentationLink(
            String aElementId,
            String aLabel,
            String aTarget,
            String aTitle) {
        super(aElementId);
        label = Objects.requireNonNull(aLabel, "Parameter aLabel must not be null.");
        target = Objects.requireNonNull(aTarget, "Parameter aTarget must not be null.");
        title = Objects.requireNonNull(aTitle, "Parameter aTitle must not be null.");
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
