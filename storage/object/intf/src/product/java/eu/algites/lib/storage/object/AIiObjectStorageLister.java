package eu.algites.lib.storage.object;

import java.io.IOException;

/** Optional enumeration capability, independent of read/write authority. */
public interface AIiObjectStorageLister extends AIiObjectStorage {

    /**
     * List matching key prefix. Prefix may be empty and must not contain path
     * traversal; page token is opaque and belongs to this query and namespace.
     * aMaxResults must be strictly positive. Implementations may return fewer.
     */
    AIiObjectStorageListPage list(
            String aKeyPrefix,
            String aPageToken,
            int aMaxResults,
            AIiObjectStorageOperationContext aContext) throws IOException;
}
