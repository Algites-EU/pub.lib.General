package eu.algites.lib.common.documentation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationTableWriteAccess}.
 * <p>
 * See {@link AIiDocumentationTable} for the read-only table contract.
 */
public class AIcDocumentationTable
        extends AIcDocumentationBlockElement
        implements AIiDocumentationTableWriteAccess {

    private final String tableTitle;

    private final List<AIiDocumentationTableColumn> columns = new ArrayList<>();

    private final List<AIiDocumentationTableRow> rows = new ArrayList<>();

    /**
     * Creates an empty documentation table.
     *
     * @param aElementId stable element identifier
     * @param aTableTitle table title, or an empty string if unavailable
     */
    public AIcDocumentationTable(
            String aElementId,
            String aTableTitle) {
        super(aElementId);
        tableTitle = Objects.requireNonNull(aTableTitle, "Parameter aTableTitle must not be null.");
    }

    @Override
    public String getTableTitle() {
        return tableTitle;
    }

    @Override
    public List<AIiDocumentationTableColumn> getColumns() {
        return Collections.unmodifiableList(columns);
    }

    @Override
    public List<AIiDocumentationTableRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    @Override
    public void addColumn(AIiDocumentationTableColumn aColumn) {
        columns.add(Objects.requireNonNull(aColumn, "Parameter aColumn must not be null."));
    }

    @Override
    public void addRow(AIiDocumentationTableRow aRow) {
        rows.add(Objects.requireNonNull(aRow, "Parameter aRow must not be null."));
    }
}
