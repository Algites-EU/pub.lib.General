package eu.algites.lib.common.documentation.model;

/**
 * Represents an explicit line break inside inline content.
 * <p>
 * A line break is different from a new paragraph. Renderers may emit an HTML
 * {@code br} element, a Markdown hard line break, or an equivalent construct in
 * their target format.
 */
public interface AIiDocumentationLineBreak extends AIiDocumentationInlineElement {
}
