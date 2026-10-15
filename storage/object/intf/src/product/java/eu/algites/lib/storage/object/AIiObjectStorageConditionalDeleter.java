package eu.algites.lib.storage.object;

import java.io.IOException;

/**
 * Optional privileged cleanup capability with exact-revision precondition.
 * Release manifests and ledger audit records must not be routinely deleted;
 * staging cleanup additionally obeys application-level retention policy.
 */
public interface AIiObjectStorageConditionalDeleter extends AIiObjectStorage {

    /**
     * Atomically delete only when the exact current revision matches.
     * Absence or mismatch causes AIxObjectStoragePreconditionFailedException.
     * Never approximate this with HEAD followed by unconditional DELETE.
     */
    void deleteIfRevisionMatches(
            String aKey,
            String aExpectedRevisionToken,
            AIiObjectStorageOperationContext aContext) throws IOException;
}
