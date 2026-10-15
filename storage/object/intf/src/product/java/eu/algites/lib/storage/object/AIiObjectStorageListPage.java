package eu.algites.lib.storage.object;

import java.util.List;

/**
 * One listing page, not a transactional namespace snapshot. Providers may
 * expose eventual listings. Never use list() to prove an object's absence.
 */
public interface AIiObjectStorageListPage {

    /** Items on this page; immutable collection. Order is provider-defined. */
    List<? extends AIiObjectStorageObjectMetadata> getObjects();

    /** Opaque continuation token, or null when enumeration has completed. */
    String getNextPageToken();
}
