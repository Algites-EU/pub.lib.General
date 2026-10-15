package eu.algites.lib.action.execution.common;

/**
 * Explicit immutable identity and scope data passed across worker threads.
 * Context-local convenience bindings such as Java ScopedValue or Python ContextVar
 * are optional implementation details and never determine transaction ownership.
 */
public interface AIiExecutionContext {

    /** Identity of the plan execution, not necessarily its definition id. */
    String getPlanExecutionId();

    /** Current immutable ownership scope. */
    AIiExecutionScope getExecutionScope();

    /** Correlation identity used for diagnostics across independent transactions. */
    String getCorrelationId();

    /** Causation identity for a spawned independent plan, or null. */
    String getCausationId();
}
