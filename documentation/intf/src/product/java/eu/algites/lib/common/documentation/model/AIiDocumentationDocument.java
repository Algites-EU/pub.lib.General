package eu.algites.lib.common.documentation.model;

/**
 * Represents the root of a documentation element tree.
 * <p>
 * A document contains top-level block elements, usually sections. It also
 * carries a human-readable document title and a document description. An empty
 * description string means that no description is available.
 */
public interface AIiDocumentationDocument extends AIiDocumentationContainerElement {

    /**
     * Returns the human-readable document title.
     *
     * @return document title
     */
    String getDocumentTitle();

    /**
     * Returns the document description.
     *
     * @return document description, or an empty string if no description is available
     */
    String getDocumentDescription();
}
