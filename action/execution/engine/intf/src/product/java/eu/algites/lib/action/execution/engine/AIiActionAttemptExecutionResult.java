package eu.algites.lib.action.execution.engine;

/**
 * Immutable record of an individual attempt including uncertainty and failures.
 * @param <AO> specialized attempt output
 */
public interface AIiActionAttemptExecutionResult<AO extends AIiActionAttemptOutput> {

    /** Positive one-based attempt index. */
    int getAttemptNumber();

    /** Domain attempt output or null if unavailable. */
    AO getOutput();

    /** Attempt outcome identifier, including UNKNOWN for unresolved remote effects. */
    String getOutcome();

    /** Structured attempt error or null. */
    eu.algites.lib.action.execution.common.AIiExecutionFailure getFailure();
}
