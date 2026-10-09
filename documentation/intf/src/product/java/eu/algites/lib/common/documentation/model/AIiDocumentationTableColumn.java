package eu.algites.lib.common.documentation.model;

/**
 * Represents metadata of a documentation table column.
 * <p>
 * The column identifier is the stable technical key. The column title is the
 * human-readable label used by renderers. An empty description string means that
 * no description is available.
 */
public interface AIiDocumentationTableColumn extends AIiDocumentationElement {

    /**
     * Returns the stable table-local column identifier.
     *
     * @return column identifier
     */
    String getColumnId();

    /**
     * Returns the human-readable column title.
     *
     * @return column title
     */
    String getColumnTitle();

    /**
     * Returns the column description.
     *
     * @return column description, or an empty string if no description is available
     */
    String getColumnDescription();
}
