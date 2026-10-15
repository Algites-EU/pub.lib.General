package eu.algites.lib.action.execution.transaction;

/**
 * Executor dedicated to a rollback action owned and scheduled by a transaction.
 * The rollback phase uses the ordinary worker/service/resource limits and emits
 * ordinary action attempt results, but its failure must not recursively trigger
 * an unlimited rollback of itself.
 * @param <IN> rollback input
 * @param <OUT> rollback output
 * @param <AO> rollback attempt output
 * @param <CTX> rollback execution context
 */
public interface AIiTransactionRollbackActionExecutor<IN extends eu.algites.lib.action.execution.engine.AIiActionInput,
        OUT extends eu.algites.lib.action.execution.engine.AIiActionOutput,
        AO extends eu.algites.lib.action.execution.engine.AIiActionAttemptOutput,
        CTX extends eu.algites.lib.action.execution.engine.AIiActionExecutionContext<?>>
        extends eu.algites.lib.action.execution.engine.AIiActionExecutor<IN, OUT, AO, CTX> {
}
