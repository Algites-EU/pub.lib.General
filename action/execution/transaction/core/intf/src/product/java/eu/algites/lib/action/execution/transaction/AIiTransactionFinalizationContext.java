package eu.algites.lib.action.execution.transaction;

/**
 * Coordinator-supplied execution context for a transaction finalization action.
 * The context provides immutable state observations; it is not a mutable plan editor.
 */
public interface AIiTransactionFinalizationContext extends eu.algites.lib.action.execution.common.AIiExecutionContext {

    /** Immutable snapshot of all actions at the current finalization boundary. */
    java.util.List<? extends AIiTransactionActionExecutionRecord> getActionRecords();

    /** Whether transaction cancellation was requested prior to finalization. */
    boolean isCancellationRequested();
}
