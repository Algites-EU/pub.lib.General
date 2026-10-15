package eu.algites.lib.storage.object;

/**
 * Receipt for one acknowledged successful write. A returned receipt proves
 * successful completion under the storage provider's durability semantics;
 * a missing receipt after an exception does NOT prove that no write occurred.
 */
public interface AIiObjectStorageWriteReceipt {

    /** Metadata for the newly committed exact object revision. */
    AIiObjectStorageObjectMetadata getMetadata();
}
