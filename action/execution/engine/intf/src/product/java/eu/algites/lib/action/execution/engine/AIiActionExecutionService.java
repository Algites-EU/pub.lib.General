package eu.algites.lib.action.execution.engine;

/**
 * Technical worker execution capacity owned by the ActionExecutionEngine.
 * The service does not own domain actions, transaction journals or commit semantics.
 * Submitted workers must not block waiting to acquire provider execution resources.
 */
public interface AIiActionExecutionService {

    /** Submits an already resource-admitted worker to the underlying execution facility. */
    <R> java.util.concurrent.CompletionStage<R> submit(AIiActionExecutionWorker<R> aWorker);

    /** Maximum number of work units admitted concurrently by this service. */
    int getMaxConcurrentWorkers();
}
