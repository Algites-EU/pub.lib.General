package eu.algites.lib.common.documentation.model;

/**
 * Represents a hierarchical documentation section.
 * <p>
 * Sections are structural block elements. A section may contain paragraphs,
 * tables, code blocks, and nested sections. An empty description string means
 * that no section description is available.
 */
public interface AIiDocumentationSection extends AIiDocumentationContainerElement {

    /**
     * Returns the human-readable section title.
     *
     * @return section title
     */
    String getSectionTitle();

    /**
     * Returns the section description.
     *
     * @return section description, or an empty string if no description is available
     */
    String getSectionDescription();
}
