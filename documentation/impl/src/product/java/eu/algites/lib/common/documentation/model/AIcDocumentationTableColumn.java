package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationTableColumnWriteAccess}.
 * <p>
 * See {@link AIiDocumentationTableColumn} for the read-only table column contract.
 */
public class AIcDocumentationTableColumn
        extends AIcDocumentationElement
        implements AIiDocumentationTableColumnWriteAccess {

    private final String columnId;

    private final String columnTitle;

    private final String columnDescription;

    /**
     * Creates a documentation table column.
     *
     * @param aElementId stable element identifier
     * @param aColumnId stable table-local column identifier
     * @param aColumnTitle human-readable column title
     * @param aColumnDescription column description, or an empty string if unavailable
     */
    public AIcDocumentationTableColumn(
            String aElementId,
            String aColumnId,
            String aColumnTitle,
            String aColumnDescription) {
        super(aElementId);
        columnId = Objects.requireNonNull(aColumnId, "Parameter aColumnId must not be null.");
        columnTitle = Objects.requireNonNull(aColumnTitle, "Parameter aColumnTitle must not be null.");
        columnDescription = Objects.requireNonNull(aColumnDescription, "Parameter aColumnDescription must not be null.");
    }

    @Override
    public String getColumnId() {
        return columnId;
    }

    @Override
    public String getColumnTitle() {
        return columnTitle;
    }

    @Override
    public String getColumnDescription() {
        return columnDescription;
    }
}
