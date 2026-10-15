package eu.algites.lib.action.execution.transaction;

/**
 * Late-bound input factory for a predeclared rollback action.
 * May inspect original/unknown external effects but must have no external side effects.
 * @param <IN> rollback input
 */
public interface AIiTransactionRollbackInputFactory<IN extends eu.algites.lib.action.execution.engine.AIiActionInput> {

    /** Resolves rollback input from the frozen failure and effects snapshot. */
    IN createRollbackInput(AIiTransactionFailureResult<?> aResult) throws Exception;
}
