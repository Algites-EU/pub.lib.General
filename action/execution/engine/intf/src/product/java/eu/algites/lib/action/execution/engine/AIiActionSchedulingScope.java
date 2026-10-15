package eu.algites.lib.action.execution.engine;

/**
 * Mutable definition-time action schedule belonging to one execution scope.
 * At execution start the entire plan freezes. After freeze no action, subtransaction,
 * listener or dependency may be added. The executor can launch a separate new plan.
 */
public interface AIiActionSchedulingScope extends eu.algites.lib.action.execution.common.AIiExecutionScope {

    /** Registers an action with a constant immutable input. */
    <IN extends AIiActionInput, OUT extends AIiActionOutput,
            AO extends AIiActionAttemptOutput, CTX extends AIiActionExecutionContext<?>>
    AIiScheduledAction<OUT, AO> scheduleAction(
            AIiActionExecutor<IN, OUT, AO, CTX> aExecutor,
            IN aInput,
            AIiActionExecutionPolicy aPolicy);

    /** Registers a typed input factory evaluated after declared dependencies resolve. */
    <IN extends AIiActionInput, OUT extends AIiActionOutput,
            AO extends AIiActionAttemptOutput, CTX extends AIiActionExecutionContext<?>>
    AIiScheduledAction<OUT, AO> scheduleAction(
            AIiActionExecutor<IN, OUT, AO, CTX> aExecutor,
            AIiActionInputFactory<IN> aInputFactory,
            AIiActionExecutionPolicy aPolicy);

    /** Declares a condition on an external action or transaction result. */
    void dependsOn(
            eu.algites.lib.action.execution.common.AIiExecutionDependencyTarget aTarget,
            eu.algites.lib.action.execution.common.AIiExecutionDependencyCondition aCondition);
}
