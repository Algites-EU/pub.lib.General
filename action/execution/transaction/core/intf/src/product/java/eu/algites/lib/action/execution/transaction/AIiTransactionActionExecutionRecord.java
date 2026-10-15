package eu.algites.lib.action.execution.transaction;

/**
 * Immutable transaction-owned record of a scheduled action, its execution outcome
 * and its final effect after rollback. Does not flatten away subtransaction lineage.
 */
public interface AIiTransactionActionExecutionRecord {

    /** Stable action identifier. */
    String getActionId();

    /** Owning transaction scope, never inferred from a current worker thread. */
    String getTransactionScopeId();

    /** Result of forward execution; may be absent if never started. */
    eu.algites.lib.action.execution.engine.AIiActionExecutionResult<?, ?>
            getExecutionResult();

    /** Rollback action result, when rollback was attempted. */
    eu.algites.lib.action.execution.engine.AIiActionExecutionResult<?, ?>
            getRollbackExecutionResult();

    /** Final external-effect state: APPLIED, REVERTED, ABSENT or UNKNOWN, etc. */
    String getEffectState();
}
