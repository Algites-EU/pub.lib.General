package eu.algites.lib.action.execution.engine;

/**
 * Domain implementation of one executable action.
 * This component does not own worker threads, the plan, or transaction commit.
 * @param <IN> action input type
 * @param <OUT> successful domain output type
 * @param <AO> specialized per-attempt output type
 * @param <CTX> specialized explicit execution context
 */
public interface AIiActionExecutor<IN extends AIiActionInput,
        OUT extends AIiActionOutput,
        AO extends AIiActionAttemptOutput,
        CTX extends AIiActionExecutionContext<?>> {

    /** Stable resolver key for reconstructing an executable operation after a restart. */
    String getExecutorId();

    /** Executes exactly one attempt under the coordinator-controlled lifecycle. */
    OUT execute(IN aInput, CTX aContext) throws Exception;
}
