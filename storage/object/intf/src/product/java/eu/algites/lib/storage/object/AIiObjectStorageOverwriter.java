package eu.algites.lib.storage.object;

import java.io.IOException;

/**
 * Explicitly privileged unconditional create-or-replace operation.
 * Not suitable for immutable releases, release ledgers, or staging objects.
 * Keeping this separate prevents accidental escalation of create-only clients.
 */
public interface AIiObjectStorageOverwriter extends AIiObjectStorage {

    /** Unconditionally replace or create a key; never used as a CAS fallback. */
    AIiObjectStorageWriteReceipt overwrite(
            String aKey,
            AIiObjectStorageContentSource aContent,
            AIiObjectStorageOperationContext aContext) throws IOException;
}
