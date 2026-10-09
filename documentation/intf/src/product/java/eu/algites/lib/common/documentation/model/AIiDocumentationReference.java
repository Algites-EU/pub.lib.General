package eu.algites.lib.common.documentation.model;

/**
 * Represents a semantic inline reference to another documented object.
 * <p>
 * A reference is different from a plain link. It keeps a structured
 * {@link AIiDocumentationReferenceTarget}, so renderers can resolve the final
 * output link, report unresolved references, or render the reference as plain
 * text when the target is unavailable.
 */
public interface AIiDocumentationReference extends AIiDocumentationInlineElement {

    /**
     * Returns the human-readable reference label.
     * <p>
     * An empty string means that a renderer may derive the visible label from
     * the reference target display name, qualified name, or target identifier.
     *
     * @return reference label, or an empty string if it should be derived
     */
    String getLabel();

    /**
     * Returns the structured reference target.
     *
     * @return reference target
     */
    AIiDocumentationReferenceTarget getReferenceTarget();

    /**
     * Returns a human-readable reference title.
     * <p>
     * An empty string means that no title is available.
     *
     * @return reference title, or an empty string if no title is available
     */
    String getTitle();
}
