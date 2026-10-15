package eu.algites.lib.storage.object;

import java.io.IOException;
import java.util.Optional;

/**
 * Direct key-based read access. A genuinely absent key returns Optional.empty().
 * Permission denial, network uncertainty, and other inability to determine
 * existence MUST throw rather than masquerade as absence.
 */
public interface AIiObjectStorageReader extends AIiObjectStorage {

    /**
     * Inspects one exact key. This operation is not an atomic pair with a
     * separate subsequent get or update; CAS resolves the intervening race.
     */
    Optional<AIiObjectStorageObjectMetadata> head(
            String aKey, AIiObjectStorageOperationContext aContext) throws IOException;

    /**
     * Opens a version-consistent stream for one exact key. The caller MUST
     * close the returned handle. Missing key is different from read failure.
     */
    Optional<AIiObjectStorageReadHandle> openRead(
            String aKey, AIiObjectStorageOperationContext aContext) throws IOException;
}
