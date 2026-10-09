package eu.algites.lib.common.documentation.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default base implementation of {@link AIiDocumentationElementWriteAccess}.
 * <p>
 * See {@link AIiDocumentationElement} for the read-only element contract.
 */
public abstract class AIcDocumentationElement implements AIiDocumentationElementWriteAccess {

    private final String elementId;

    private final Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * Creates a documentation element.
     *
     * @param aElementId stable element identifier
     */
    protected AIcDocumentationElement(String aElementId) {
        elementId = Objects.requireNonNull(aElementId, "Parameter aElementId must not be null.");
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getMetadataValue(String aKey) {
        Objects.requireNonNull(aKey, "Parameter aKey must not be null.");
        return metadata.get(aKey);
    }

    @Override
    public void putMetadataValue(String aKey, String aValue) {
        Objects.requireNonNull(aKey, "Parameter aKey must not be null.");
        Objects.requireNonNull(aValue, "Parameter aValue must not be null.");
        metadata.put(aKey, aValue);
    }

    @Override
    public void removeMetadataValue(String aKey) {
        Objects.requireNonNull(aKey, "Parameter aKey must not be null.");
        metadata.remove(aKey);
    }
}
