package eu.algites.lib.action.execution.resource;

/**
 * Immutable amount of a shared ExecutionResource needed by an operation.
 * Resolved from the endpoint or provider configuration before plan freeze.
 */
public interface AIiExecutionResourceRequirement {

    /** Resource identity registered with the shared execution engine. */
    String getResourceId();

    /** Positive units that must be reserved simultaneously with other requirements. */
    int getRequiredUsages();
}
