package eu.algites.lib.action.execution.transaction;

/**
 * Transaction-aware specialization of the general action scheduler.
 * Transaction results are reconciled from the journal without owning worker threads.
 */
public interface AIiTransactionalActionExecutionEngine extends eu.algites.lib.action.execution.engine.AIiActionExecutionEngine {

    /** Creates a transaction-aware static definition. */
    @Override
    AIiTransactionalActionExecutionPlan createPlan();
}
