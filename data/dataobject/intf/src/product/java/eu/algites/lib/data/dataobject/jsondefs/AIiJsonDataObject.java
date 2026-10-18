package eu.algites.lib.data.dataobject.jsondefs;

import eu.algites.lib.data.dataobject.AIiDataObject;

/**
 * Marks a read-only data-object contract originating from JSON Schema.
 *
 * <p>The marker identifies the source definition technology without imposing a
 * serialization format, storage strategy or mutability requirements. Generated
 * read-only contracts extend this interface; a mutable counterpart additionally
 * implements the matching technology-specific SmartDataObject contract.
 */
public interface AIiJsonDataObject extends AIiDataObject {
}
