package eu.algites.lib.common.documentation.model.impl.standard;


import eu.algites.lib.common.documentation.model.AIiDocumentationInlineElement;
import eu.algites.lib.common.documentation.model.AIiDocumentationReferenceTarget;
import eu.algites.lib.common.documentation.model.AIiDocumentationTableCell;
import eu.algites.lib.common.documentation.model.AIiDocumentationTableCellWriteAccess;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationTableCellWriteAccess}.
 * <p>
 * See {@link AIiDocumentationTableCell} for the read-only table cell contract.
 */
public class AIcDocumentationTableCell
        extends AIcDocumentationElement
        implements AIiDocumentationTableCellWriteAccess {

    private final String columnId;

    private final List<AIiDocumentationInlineElement> inlineElements = new ArrayList<>();

    /**
     * Creates an empty table cell.
     *
     * @param aElementId stable element identifier
     * @param aColumnId identifier of the column this cell belongs to
     */
    public AIcDocumentationTableCell(
            String aElementId,
            String aColumnId) {
        super(aElementId);
        columnId = Objects.requireNonNull(aColumnId, "Parameter aColumnId must not be null.");
    }

    /**
     * Creates a table cell initialized with inline elements.
     *
     * @param aElementId stable element identifier
     * @param aColumnId identifier of the column this cell belongs to
     * @param aInlineElements initial inline elements in rendering order
     */
    public AIcDocumentationTableCell(
            String aElementId,
            String aColumnId,
            List<? extends AIiDocumentationInlineElement> aInlineElements) {
        this(aElementId, aColumnId);
        Objects.requireNonNull(aInlineElements, "Parameter aInlineElements must not be null.");

        for (AIiDocumentationInlineElement locInlineElement : aInlineElements) {
            inlineElements.add(Objects.requireNonNull(
                    locInlineElement,
                    "Inline element must not be null."));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnId() {
        return columnId;
    }

    /** {@inheritDoc} */
    @Override
    public List<AIiDocumentationInlineElement> getInlineElements() {
        return Collections.unmodifiableList(inlineElements);
    }

    /** {@inheritDoc} */
    @Override
    public void addInlineElement(AIiDocumentationInlineElement aInlineElement) {
        inlineElements.add(Objects.requireNonNull(aInlineElement, "Parameter aInlineElement must not be null."));
    }

    /** {@inheritDoc} */
    @Override
    public void addInlineElements(List<? extends AIiDocumentationInlineElement> aInlineElements) {
        Objects.requireNonNull(aInlineElements, "Parameter aInlineElements must not be null.");

        for (AIiDocumentationInlineElement locInlineElement : aInlineElements) {
            addInlineElement(locInlineElement);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addText(
            String aElementId,
            String aText) {
        addInlineElement(new AIcDocumentationText(aElementId, aText));
    }

    /** {@inheritDoc} */
    @Override
    public void addLineBreak(String aElementId) {
        addInlineElement(new AIcDocumentationLineBreak(aElementId));
    }

    /** {@inheritDoc} */
    @Override
    public void addLink(
            String aElementId,
            String aLabel,
            String aTarget,
            String aTitle) {
        addInlineElement(new AIcDocumentationLink(aElementId, aLabel, aTarget, aTitle));
    }

    /** {@inheritDoc} */
    @Override
    public void addReference(
            String aElementId,
            String aLabel,
            AIiDocumentationReferenceTarget aReferenceTarget,
            String aTitle) {
        addInlineElement(new AIcDocumentationReference(aElementId, aLabel, aReferenceTarget, aTitle));
    }

    /** {@inheritDoc} */
    @Override
    public void addInlineCode(
            String aElementId,
            String aCode) {
        addInlineElement(new AIcDocumentationInlineCode(aElementId, aCode));
    }
}
