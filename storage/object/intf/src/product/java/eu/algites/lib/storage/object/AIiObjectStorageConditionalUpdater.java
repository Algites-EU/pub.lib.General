package eu.algites.lib.storage.object;

import java.io.IOException;

/**
 * Exact-key atomic compare-and-swap, required for release coordination.
 * The expected opaque token comes from the same backend's observed metadata.
 * Expiration, retry, or stale tokens cannot silently fall back to overwrite.
 */
public interface AIiObjectStorageConditionalUpdater extends AIiObjectStorage {

    /**
     * Replace an existing object only when its current revision matches the
     * expected token. If absent or different, throw the definitive
     * AIxObjectStoragePreconditionFailedException. Never implement as
     * HEAD followed by unconditional PUT.
     */
    AIiObjectStorageWriteReceipt replaceIfRevisionMatches(
            String aKey,
            String aExpectedRevisionToken,
            AIiObjectStorageContentSource aContent,
            AIiObjectStorageOperationContext aContext) throws IOException;
}
