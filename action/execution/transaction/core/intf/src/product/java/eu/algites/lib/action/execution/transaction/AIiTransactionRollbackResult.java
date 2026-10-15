package eu.algites.lib.action.execution.transaction;

/**
 * Immutable aggregate account of all rollback attempts and remaining effects.
 * A request to cancel running actions is NOT proof that remote effects stopped.
 */
public interface AIiTransactionRollbackResult {

    /** Whether every relevant external effect is known to be resolved. */
    boolean isComplete();

    /** Whether any external effect remains unknown after rollback. */
    boolean hasUnknownEffects();

    /** Per-action execution and rollback records including error details. */
    java.util.List<? extends AIiTransactionActionExecutionRecord> getActionRecords();
}
