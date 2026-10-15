package eu.algites.lib.action.execution.transaction;

/**
 * Transaction-enabled static plan definition.
 * All transaction scopes, actions, dependencies and finalizers are created before freeze.
 * No JOINED/INDEPENDENT subtransaction switch exists: independent means top-level.
 */
public interface AIiTransactionalActionExecutionPlan extends eu.algites.lib.action.execution.engine.AIiActionExecutionPlan {

    /** Adds an independent root transaction during the definition phase. */
    AIiExecutionTransaction createTransaction(String aTransactionId);
}
