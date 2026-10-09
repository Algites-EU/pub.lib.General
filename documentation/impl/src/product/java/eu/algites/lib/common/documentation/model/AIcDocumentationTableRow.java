package eu.algites.lib.common.documentation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationTableRowWriteAccess}.
 * <p>
 * See {@link AIiDocumentationTableRow} for the read-only table row contract.
 */
public class AIcDocumentationTableRow
        extends AIcDocumentationElement
        implements AIiDocumentationTableRowWriteAccess {

    private final List<AIiDocumentationTableCell> cells = new ArrayList<>();

    /**
     * Creates an empty documentation table row.
     *
     * @param aElementId stable element identifier
     */
    public AIcDocumentationTableRow(String aElementId) {
        super(aElementId);
    }

    @Override
    public List<AIiDocumentationTableCell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    @Override
    public void addCell(AIiDocumentationTableCell aCell) {
        cells.add(Objects.requireNonNull(aCell, "Parameter aCell must not be null."));
    }
}
