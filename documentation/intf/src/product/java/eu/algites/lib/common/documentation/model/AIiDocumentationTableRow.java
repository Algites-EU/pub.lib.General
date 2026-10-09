package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Represents a row in a documentation table.
 * <p>
 * A row contains cells. The cells may be stored in rendering order, but their
 * meaning is determined by the column identifiers stored on individual cells.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify the cell list should use
 * {@link AIiDocumentationTableRowWriteAccess}.
 */
public interface AIiDocumentationTableRow extends AIiDocumentationElement {

    /**
     * Returns row cells.
     *
     * @return read-only ordered cell list
     */
    List<AIiDocumentationTableCell> getCells();
}
