package eu.algites.lib.common.documentation.model;

/**
 * Provides write access to a renderer-neutral documentation element.
 * <p>
 * The read-only contract is defined by {@link AIiDocumentationElement}. This
 * capability interface is intended for construction, aggregation, and
 * model-processing phases. Validators, renderers, and other read-only consumers
 * should depend on {@link AIiDocumentationElement} instead.
 */
public interface AIiDocumentationElementWriteAccess extends AIiDocumentationElement {

    /**
     * Adds or replaces a metadata value.
     *
     * @param aKey metadata key
     * @param aValue metadata value
     */
    void putMetadataValue(String aKey, String aValue);

    /**
     * Removes a metadata value.
     *
     * @param aKey metadata key
     */
    void removeMetadataValue(String aKey);
}
