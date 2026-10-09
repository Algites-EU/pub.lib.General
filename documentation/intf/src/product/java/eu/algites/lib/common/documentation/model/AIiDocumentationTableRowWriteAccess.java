package eu.algites.lib.common.documentation.model;

/**
 * Provides write access to a documentation table row.
 * <p>
 * The read-only contract is defined by {@link AIiDocumentationTableRow}. This
 * capability interface is intended for construction, aggregation, and
 * model-processing phases. Validators, renderers, and other read-only consumers
 * should depend on the read-only interface instead.
 */
public interface AIiDocumentationTableRowWriteAccess
        extends AIiDocumentationTableRow,
        AIiDocumentationElementWriteAccess {

    /**
     * Adds a cell at the end of the row.
     *
     * @param aCell cell to add
     */
    void addCell(AIiDocumentationTableCell aCell);
}
