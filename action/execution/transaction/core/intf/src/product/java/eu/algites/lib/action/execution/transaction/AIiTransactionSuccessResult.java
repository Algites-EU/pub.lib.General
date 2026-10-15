package eu.algites.lib.action.execution.transaction;

/**
 * Final success-only view supplied after successful top-level transaction commit.
 * Implementations guarantee a committed outcome, rather than asking callers to cast.
 * @param <CO> typed commit action output
 */
public interface AIiTransactionSuccessResult<CO extends eu.algites.lib.action.execution.engine.AIiActionOutput>
        extends AIiTransactionExecutionResult<CO> {

    /** Successful commit output, which cannot be missing for a conforming successful action. */
    CO getCommittedOutput();
}
