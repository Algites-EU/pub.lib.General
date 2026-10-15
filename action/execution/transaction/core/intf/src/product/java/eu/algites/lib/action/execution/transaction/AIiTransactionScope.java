package eu.algites.lib.action.execution.transaction;

/**
 * Joinable nested execution scope sharing one root transaction journal and rollback fate.
 * A subtransaction has its own locally ordered schedule, but cannot independently
 * commit externally visible effects of the root transaction.
 */
public interface AIiTransactionScope extends eu.algites.lib.action.execution.engine.AIiActionSchedulingScope {

    /** Returns the owning top-level transaction, independent of worker threads. */
    AIiExecutionTransaction getRootTransaction();

    /** Creates a statically configured child scope before plan freeze. */
    AIiExecutionSubtransaction createSubtransaction(String aScopeId);

    /** Installs a predeclared finalization strategy during plan definition. */
    <CI extends eu.algites.lib.action.execution.engine.AIiActionInput,
            CO extends eu.algites.lib.action.execution.engine.AIiActionOutput,
            FC extends AIiTransactionFinalizationContext,
            AF extends AIiTransactionActionFailureContext,
            CF extends AIiTransactionCommitFailureContext<CO>>
    void setFinalizationAction(AIiTransactionFinalizationAction<CI, CO, FC, AF, CF> aAction,
            eu.algites.lib.action.execution.engine.AIiActionInputFactory<CI> aCommitInputFactory);
}
