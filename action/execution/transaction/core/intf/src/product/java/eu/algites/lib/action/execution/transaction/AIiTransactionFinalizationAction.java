package eu.algites.lib.action.execution.transaction;

/**
 * Preconfigured transaction-local finalization strategy.
 * The commit method creates the intended final effect; onActionFailure handles
 * pre-commit action failures, and onCommitFailure handles commitAction failures.
 * They are not after-transaction observers. Exceptions are journaled and may
 * leave the transaction in an incomplete/unknown rollback state.
 * @param <CI> commit input type
 * @param <CO> commit output type
 * @param <FC> specialized commit execution context
 * @param <AF> specialized action-failure context
 * @param <CF> specialized commit-failure context
 */
public interface AIiTransactionFinalizationAction<CI extends eu.algites.lib.action.execution.engine.AIiActionInput,
        CO extends eu.algites.lib.action.execution.engine.AIiActionOutput,
        FC extends AIiTransactionFinalizationContext,
        AF extends AIiTransactionActionFailureContext,
        CF extends AIiTransactionCommitFailureContext<CO>> {

    /** Executes the configured commit action after ordinary scheduled work succeeds. */
    CO commit(CI aInput, FC aContext) throws Exception;

    /** Handles an abort caused by a non-ignorable scheduled-action failure. */
    void onActionFailure(AF aContext) throws Exception;

    /** Handles a failure from commit and receives its typed attempt history. */
    void onCommitFailure(CF aContext) throws Exception;
}
