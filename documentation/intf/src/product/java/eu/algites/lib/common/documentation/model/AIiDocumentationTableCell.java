package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Represents a cell in a documentation table row.
 * <p>
 * The cell references a column by its column identifier and stores ordered
 * inline content. This allows table cells to contain not only plain text, but
 * also links, semantic references, line breaks, and inline code.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify inline content should use
 * {@link AIiDocumentationTableCellWriteAccess}.
 */
public interface AIiDocumentationTableCell extends AIiDocumentationElement {

    /**
     * Returns the identifier of the column this cell belongs to.
     *
     * @return table-local column identifier
     */
    String getColumnId();

    /**
     * Returns cell inline elements in their rendering order.
     *
     * @return read-only ordered inline element list
     */
    List<AIiDocumentationInlineElement> getInlineElements();
}
