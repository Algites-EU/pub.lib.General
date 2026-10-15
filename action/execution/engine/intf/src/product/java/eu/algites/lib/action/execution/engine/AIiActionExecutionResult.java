package eu.algites.lib.action.execution.engine;

/**
 * Frozen execution record for one action; separate from its domain ActionOutput.
 * A succeeded execution can later have its effects rolled back by the owning transaction.
 * @param <OUT> final domain output type
 * @param <AO> specialized attempt output type
 */
public interface AIiActionExecutionResult<OUT extends AIiActionOutput, AO extends AIiActionAttemptOutput>
        extends eu.algites.lib.action.execution.common.AIiExecutionTargetResult {

    /** Stable scheduled action identity, including retries. */
    String getActionId();

    /** Stable provider executor identifier. */
    String getExecutorId();

    /** Domain output if available; absent on some failure paths. */
    OUT getOutput();

    /** Immutable chronological attempt record. */
    java.util.List<? extends AIiActionAttemptExecutionResult<AO>> getAttempts();
}
