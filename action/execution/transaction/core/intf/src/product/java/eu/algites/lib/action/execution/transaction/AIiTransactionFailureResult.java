package eu.algites.lib.action.execution.transaction;

/**
 * Final failure-only view produced AFTER the rollback attempt and reconciliation.
 * It includes action failures, attempted commit if any, and rollback completeness.
 * @param <CO> typed commit action output if it was produced
 */
public interface AIiTransactionFailureResult<CO extends eu.algites.lib.action.execution.engine.AIiActionOutput>
        extends AIiTransactionExecutionResult<CO> {

    /** Definitive reason for failure including its original phase. */
    eu.algites.lib.action.execution.common.AIiExecutionFailure getPrimaryFailure();
}
