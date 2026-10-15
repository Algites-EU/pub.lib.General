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

    public AIxObjectStoragePreconditionFailedException(String aMessage) {
        super(aMessage);
    }

    public AIxObjectStoragePreconditionFailedException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }
}
