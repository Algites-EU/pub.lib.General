package eu.algites.lib.common.documentation.model;

import java.util.Map;

/**
 * Represents a common renderer-neutral documentation element.
 * <p>
 * Each element has a stable element identifier and optional string metadata.
 * The element identifier can be used by renderers for anchors, cross-links,
 * diagnostics, source mapping, or stable incremental generation.
 * <p>
 * This interface provides read-only access. Construction and transformation
 * code that needs to modify an element should use
 * {@link AIiDocumentationElementWriteAccess}.
 */
public interface AIiDocumentationElement {

    /**
     * Returns the stable identifier of this documentation element.
     * <p>
     * The identifier should be stable across repeated generation runs whenever
     * the represented source object stays the same.
     *
     * @return stable element identifier
     */
    String getElementId();

    /**
     * Returns metadata associated with this documentation element.
     * <p>
     * The returned map must not allow callers to modify the element. Metadata is
     * intended for renderer-neutral annotations, diagnostics, source mapping,
     * and extension points.
     *
     * @return read-only element metadata keyed by metadata name
     */
    Map<String, String> getMetadata();

    /**
     * Returns a single metadata value.
     *
     * @param aKey metadata key
     * @return metadata value, or {@code null} if no value exists for the key
     */
    String getMetadataValue(String aKey);
}
