package eu.algites.lib.action.execution.transaction;

/**
 * Final immutable transaction outcome, including hierarchical action history.
 * A child scope can finish execution successfully but later have its effects rolled
 * back when another child fails. Never conflate these two observations.
 * @param <CO> output of the commit action when available
 */
public interface AIiTransactionExecutionResult<CO extends eu.algites.lib.action.execution.engine.AIiActionOutput>
        extends eu.algites.lib.action.execution.common.AIiExecutionTargetResult {

    /** Commit action result, including attempts and partial output, if attempted. */
    eu.algites.lib.action.execution.engine.AIiActionExecutionResult<CO, ?>
            getCommitActionResult();

    /** Immutable action records for the whole owned subtree. */
    java.util.List<? extends AIiTransactionActionExecutionRecord> getActionRecords();

    /** Immutable child scope execution results. */
    java.util.List<? extends AIiTransactionExecutionResult<?>> getSubtransactionResults();

    /** Rollback report or null when no rollback occurred. */
    AIiTransactionRollbackResult getRollbackResult();

    /** Lifecycle phase in which the first non-ignorable error occurred. */
    String getFailurePhase();
}
