package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationSectionWriteAccess}.
 * <p>
 * See {@link AIiDocumentationSection} for the read-only section contract.
 */
public class AIcDocumentationSection
        extends AIcDocumentationContainerElement
        implements AIiDocumentationSectionWriteAccess {

    private final String sectionTitle;

    private final String sectionDescription;

    /**
     * Creates a documentation section.
     *
     * @param aElementId stable element identifier
     * @param aSectionTitle human-readable section title
     * @param aSectionDescription section description, or an empty string if unavailable
     */
    public AIcDocumentationSection(
            String aElementId,
            String aSectionTitle,
            String aSectionDescription) {
        super(aElementId);
        sectionTitle = Objects.requireNonNull(aSectionTitle, "Parameter aSectionTitle must not be null.");
        sectionDescription = Objects.requireNonNull(aSectionDescription, "Parameter aSectionDescription must not be null.");
    }

    @Override
    public String getSectionTitle() {
        return sectionTitle;
    }

    @Override
    public String getSectionDescription() {
        return sectionDescription;
    }
}
