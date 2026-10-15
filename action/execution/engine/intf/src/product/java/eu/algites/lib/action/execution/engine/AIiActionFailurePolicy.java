package eu.algites.lib.action.execution.engine;

/**
 * Immutable policy for a non-ignorable failed action.
 * A failure marked as aborting stops further unscheduled work in the owning
 * transaction and requests cancellation of its currently running actions.
 */
public interface AIiActionFailurePolicy {

    /** True if definitive failure must abort and roll back the owning transaction. */
    boolean abortTransactionOnFailure();
}
