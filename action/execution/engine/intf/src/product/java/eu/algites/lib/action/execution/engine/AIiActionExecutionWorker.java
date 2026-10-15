package eu.algites.lib.action.execution.engine;

/**
 * One short-lived technical work unit executed by ActionExecutionService.
 * It is not a permanently allocated thread and not the domain ActionExecutor.
 * @param <R> result returned to the scheduling service
 */
public interface AIiActionExecutionWorker<R> {

    /** Runs the coordinator-prepared work and returns or throws. */
    R execute() throws Exception;
}
