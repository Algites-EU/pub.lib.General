package eu.algites.lib.action.execution.transaction;

/**
 * Independent top-level transaction owning final commit, rollback and journal.
 * The transaction is a scope for multiple concurrently executing actions, not a thread.
 * Execution dependencies across top-level transactions do not merge rollback fates.
 */
public interface AIiExecutionTransaction extends AIiTransactionScope, eu.algites.lib.action.execution.common.AIiExecutionDependencyTarget {

    /** Final-result observer bound at definition time and executed independently. */
    <TSR extends AIiTransactionSuccessResult<?>,
            TFR extends AIiTransactionFailureResult<?>, CTX extends AIiAfterTransactionContext>
    void setAfterTransactionAction(AIiAfterTransactionAction<TSR, TFR, CTX> aAction);

    /** Predeclared rollback strategy for the whole transaction when applicable. */
    <IN extends eu.algites.lib.action.execution.engine.AIiActionInput,
            OUT extends eu.algites.lib.action.execution.engine.AIiActionOutput,
            AO extends eu.algites.lib.action.execution.engine.AIiActionAttemptOutput,
            CTX extends eu.algites.lib.action.execution.engine.AIiActionExecutionContext<?>>
    void setRollbackAction(AIiTransactionRollbackActionDefinition<IN, OUT, AO, CTX> aAction);
}
