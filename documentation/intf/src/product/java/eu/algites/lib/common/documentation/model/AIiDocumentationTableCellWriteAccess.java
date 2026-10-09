package eu.algites.lib.common.documentation.model;

import java.util.List;

/**
 * Provides write access to a documentation table cell.
 * <p>
 * The read-only contract is defined by {@link AIiDocumentationTableCell}. This
 * capability interface is intended for construction, aggregation, and
 * model-processing phases. Validators, renderers, and other read-only consumers
 * should depend on the read-only interface instead.
 */
public interface AIiDocumentationTableCellWriteAccess
        extends AIiDocumentationTableCell,
        AIiDocumentationElementWriteAccess {

    /**
     * Adds an inline element at the end of the cell.
     *
     * @param aInlineElement inline element to add
     */
    void addInlineElement(AIiDocumentationInlineElement aInlineElement);

    /**
     * Adds multiple inline elements in the provided order.
     *
     * @param aInlineElements inline elements to add
     */
    void addInlineElements(List<? extends AIiDocumentationInlineElement> aInlineElements);

    /**
     * Creates and adds a plain text inline element.
     *
     * @param aElementId stable identifier of the created text element
     * @param aText plain text content
     */
    void addText(String aElementId, String aText);

    /**
     * Creates and adds an explicit line break.
     *
     * @param aElementId stable identifier of the created line break element
     */
    void addLineBreak(String aElementId);

    /**
     * Creates and adds a non-semantic inline link.
     *
     * @param aElementId stable identifier of the created link element
     * @param aLabel human-readable link label
     * @param aTarget renderer-neutral link target
     * @param aTitle human-readable link title, or an empty string if unavailable
     */
    void addLink(String aElementId, String aLabel, String aTarget, String aTitle);

    /**
     * Creates and adds a semantic inline reference.
     *
     * @param aElementId stable identifier of the created reference element
     * @param aLabel human-readable reference label, or an empty string if it should be derived
     * @param aReferenceTarget structured semantic reference target
     * @param aTitle human-readable reference title, or an empty string if unavailable
     */
    void addReference(
            String aElementId,
            String aLabel,
            AIiDocumentationReferenceTarget aReferenceTarget,
            String aTitle);

    /**
     * Creates and adds an inline code element.
     *
     * @param aElementId stable identifier of the created inline code element
     * @param aCode inline code text
     */
    void addInlineCode(String aElementId, String aCode);
}
