package eu.algites.lib.storage.object;

import java.io.IOException;

/**
 * A definitive failed atomic storage precondition. No mutation from this
 * operation was committed. Typical cases: key already exists for create-only;
 * key absent or its revision token mismatches for CAS.
 *
 * <p>All other write failures, including network timeout and cancellation,
 * must be treated as potentially committed until reconciliation proves otherwise.</p>
 */
public class AIxObjectStoragePreconditionFailedException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a object storage precondition failed exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     */
    public AIxObjectStoragePreconditionFailedException(String aMessage) {
        super(aMessage);
    }

    /**
     * Creates a object storage precondition failed exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     * @param aCause underlying cause of the failure
     */
    public AIxObjectStoragePreconditionFailedException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }
}
