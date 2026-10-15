package eu.algites.lib.storage.object;

import java.io.IOException;

/**
 * Atomically creates new immutable object keys without overwriting existing
 * bytes. The condition must hold at the storage provider's linearization point,
 * not merely after a client-side HEAD check.
 */
public interface AIiObjectStorageCreateOnlyWriter extends AIiObjectStorage {

    /**
     * Atomically create the key only when absent. On existing key throw
     * AIxObjectStoragePreconditionFailedException without modifying it.
     * A timeout/cancellation/unknown response must NOT be converted to
     * precondition failure or success; the caller must reconcile by read.
     */
    AIiObjectStorageWriteReceipt createIfAbsent(
            String aKey,
            AIiObjectStorageContentSource aContent,
            AIiObjectStorageOperationContext aContext) throws IOException;
}
