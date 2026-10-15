package eu.algites.lib.action.execution.transaction;

/**
 * Predeclared rollback executor and late-bound rollback input factory.
 * Exactly which action effects require undo is decided from immutable journal
 * state, not by modifying the plan after it has started.
 * @param <IN> rollback input
 * @param <OUT> rollback output
 * @param <AO> rollback attempt output
 * @param <CTX> rollback context
 */
public interface AIiTransactionRollbackActionDefinition<IN extends eu.algites.lib.action.execution.engine.AIiActionInput,
        OUT extends eu.algites.lib.action.execution.engine.AIiActionOutput,
        AO extends eu.algites.lib.action.execution.engine.AIiActionAttemptOutput,
        CTX extends eu.algites.lib.action.execution.engine.AIiActionExecutionContext<?>> {

    /** Statically configured executable rollback strategy. */
    AIiTransactionRollbackActionExecutor<IN, OUT, AO, CTX> getExecutor();

    /** Typed input factory bound to immutable forward-action effects. */
    AIiTransactionRollbackInputFactory<IN> getInputFactory();
}
