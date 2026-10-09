package eu.algites.lib.common.documentation.model;

/**
 * Provides write access to a documentation table.
 * <p>
 * The read-only contract is defined by {@link AIiDocumentationTable}. This
 * capability interface is intended for construction, aggregation, and
 * model-processing phases. Validators, renderers, and other read-only consumers
 * should depend on the read-only interface instead.
 */
public interface AIiDocumentationTableWriteAccess
        extends AIiDocumentationTable,
        AIiDocumentationBlockElementWriteAccess {

    /**
     * Adds a table column at the end of the column list.
     *
     * @param aColumn column to add
     */
    void addColumn(AIiDocumentationTableColumn aColumn);

    /**
     * Adds a table row at the end of the row list.
     *
     * @param aRow row to add
     */
    void addRow(AIiDocumentationTableRow aRow);
}
