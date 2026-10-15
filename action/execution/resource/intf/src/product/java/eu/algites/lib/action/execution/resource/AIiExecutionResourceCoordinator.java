package eu.algites.lib.action.execution.resource;

/**
 * Thread-safe global authority over capacity in one ActionExecutionEngine instance.
 * tryReserve never blocks a worker. It either grants ALL requested resources
 * atomically or returns null; the scheduler may then dispatch another action.
 * A distributed implementation is required to enforce limits across multiple JVMs.
 */
public interface AIiExecutionResourceCoordinator {

    /** Registers a resource or validates agreement with an existing identity. */
    void registerResource(AIiExecutionResource aResource);

    /** Atomically reserves all requirements; returns null if unavailable. */
    AIiExecutionResourceLease tryReserve(
            java.util.List<? extends AIiExecutionResourceRequirement> aRequirements);

    /** Takes a thread-safe immutable usage snapshot for observability and planning. */
    java.util.List<? extends AIiExecutionResourceSnapshot> snapshot();
}
