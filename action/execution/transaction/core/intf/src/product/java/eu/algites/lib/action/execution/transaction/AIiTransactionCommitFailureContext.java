package eu.algites.lib.action.execution.transaction;

/**
 * Structured snapshot for a failure thrown by the commit action.
 * The commit action must not attempt ad-hoc repair within its own commit method.
 * It may still have yielded a partial or uncertain output and effect.
 * @param <CO> typed commit output when present
 */
public interface AIiTransactionCommitFailureContext<CO extends eu.algites.lib.action.execution.engine.AIiActionOutput>
        extends AIiTransactionFinalizationContext {

    /** Immutable full commit action result, including failed attempts and optional output. */
    eu.algites.lib.action.execution.engine.AIiActionExecutionResult<CO, ?>
            getCommitExecutionResult();

    /** Primary commit execution error, including unknown-effect status when appropriate. */
    eu.algites.lib.action.execution.common.AIiExecutionFailure getCommitFailure();
}
