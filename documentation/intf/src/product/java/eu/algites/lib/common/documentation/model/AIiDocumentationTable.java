package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Represents a tabular block element.
 * <p>
 * A table consists of column definitions and rows. Cells are associated with
 * columns through column identifiers, which allows renderers to keep cell
 * values stable even if column order changes. Cell content may contain inline
 * elements such as links and semantic references.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify the table should use
 * {@link AIiDocumentationTableWriteAccess}.
 */
public interface AIiDocumentationTable extends AIiDocumentationBlockElement {

    /**
     * Returns the human-readable table title.
     * <p>
     * An empty string means that no title is available.
     *
     * @return table title, or an empty string if no title is available
     */
    String getTableTitle();

    /**
     * Returns table columns in their rendering order.
     *
     * @return read-only ordered column list
     */
    List<AIiDocumentationTableColumn> getColumns();

    /**
     * Returns table rows in their rendering order.
     *
     * @return read-only ordered row list
     */
    List<AIiDocumentationTableRow> getRows();
}
