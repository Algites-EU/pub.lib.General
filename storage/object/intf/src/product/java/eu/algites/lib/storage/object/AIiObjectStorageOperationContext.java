package eu.algites.lib.storage.object;

import java.time.Instant;

/**
 * Independent per-operation cancellation and deadline view.
 * May be adapted from an ActionExecutionContext but never grants the storage
 * access to the action scheduler, mutable plan or transaction coordinator.
 */
public interface AIiObjectStorageOperationContext {

    /** Deadline, or null if the caller has not imposed one. */
    Instant getDeadline();

    /**
     * Cooperative cancellation signal, checked at transfer boundaries as well
     * as before a request. Cancellation does not prove a remote write was undone.
     */
    boolean isCancellationRequested();

    /** Nonsecret correlation identifier, or null. Must not contain credentials. */
    String getCorrelationId();
}
