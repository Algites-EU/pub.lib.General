package eu.algites.lib.action.execution.transaction;

/**
 * Structured snapshot for a non-ignorable failure of a normal scheduled action.
 * The commit action has not run, so no commit output can be presumed.
 */
public interface AIiTransactionActionFailureContext extends AIiTransactionFinalizationContext {

    /** Primary execution failure triggering transaction abort. */
    eu.algites.lib.action.execution.common.AIiExecutionFailure getActionFailure();

    /** Stable id of action whose failure triggered abort. */
    String getFailedActionId();
}
