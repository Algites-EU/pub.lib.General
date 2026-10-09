package eu.algites.lib.common.documentation.model;

/**
 * Represents a non-semantic inline link.
 * <p>
 * Links are intended for external URLs, generated document paths, anchors, or
 * other renderer-understandable textual targets. References to documented
 * source objects should use {@link AIiDocumentationReference} instead, so that a
 * renderer can resolve them through a documentation reference resolver.
 */
public interface AIiDocumentationLink extends AIiDocumentationInlineElement {

    /**
     * Returns the human-readable link label.
     *
     * @return link label
     */
    String getLabel();

    /**
     * Returns the link target.
     * <p>
     * The target is renderer-neutral text, for example an absolute URL, a
     * relative path, or an anchor expression understood by the renderer.
     *
     * @return link target
     */
    String getTarget();

    /**
     * Returns a human-readable link title.
     * <p>
     * An empty string means that no title is available.
     *
     * @return link title, or an empty string if no title is available
     */
    String getTitle();
}
