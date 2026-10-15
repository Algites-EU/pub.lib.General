package eu.algites.lib.action.execution.engine;

/**
 * Immutable result of a full execution plan containing root results.
 * Independent top-level transactions retain independent final outcomes.
 */
public interface AIiActionExecutionPlanResult {

    /** Plan execution identity. */
    String getPlanExecutionId();

    /** Results for all statically configured root scopes. */
    java.util.List<? extends eu.algites.lib.action.execution.common.AIiExecutionTargetResult>
            getRootResults();

    /** Whether the plan execution has reached a terminal state. */
    boolean isTerminal();
}
