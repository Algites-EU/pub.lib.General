package eu.algites.lib.action.execution.engine;

/**
 * Engine-wide scheduler and executor entry point.
 * Execution is reactive over a fully static frozen plan. It admits actions only
 * when ordering, dependencies, worker capacity and ALL endpoint capacities allow.
 * No artificial global maximum number of dormant transactions is imposed.
 */
public interface AIiActionExecutionEngine {

    /** Creates a new plan definition that is not yet executing. */
    AIiActionExecutionPlan createPlan();

    /** Access to the shared worker capacity service. */
    AIiActionExecutionService getExecutionService();

    /** Access to global capacity coordinator shared by all plans in this engine. */
    eu.algites.lib.action.execution.resource.AIiExecutionResourceCoordinator
            getExecutionResourceCoordinator();

    /** Freezes and executes the plan, waiting for its final result. */
    AIiActionExecutionPlanResult execute(AIiActionExecutionPlan aPlan) throws Exception;

    /** Starts the immutable plan without blocking the initiating caller. */
    java.util.concurrent.CompletionStage<AIiActionExecutionPlanResult> executeAsync(
            AIiActionExecutionPlan aPlan);
}
