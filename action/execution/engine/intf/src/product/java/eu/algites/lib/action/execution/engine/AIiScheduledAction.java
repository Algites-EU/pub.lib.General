package eu.algites.lib.action.execution.engine;

/**
 * Frozen-plan handle for a future action result and dependencies.
 * Dependencies are registered before plan execution and never alter ownership.
 * @param <OUT> expected action output type
 * @param <AO> specialized attempt output type
 */
public interface AIiScheduledAction<OUT extends AIiActionOutput, AO extends AIiActionAttemptOutput>
        extends eu.algites.lib.action.execution.common.AIiExecutionDependencyTarget {

    /** Register a prerequisite before freeze; append semantics for repeated calls. */
    void dependsOn(
            eu.algites.lib.action.execution.common.AIiExecutionDependencyTarget aTarget,
            eu.algites.lib.action.execution.common.AIiExecutionDependencyCondition aCondition);

    /** Immutable scheduling policy. */
    AIiActionExecutionPolicy getPolicy();

    /** Frozen endpoint-derived resource requirements; not configurable on the action. */
    java.util.List<? extends eu.algites.lib.action.execution.resource.AIiExecutionResourceRequirement>
            getExecutionResourceRequirements();
}
