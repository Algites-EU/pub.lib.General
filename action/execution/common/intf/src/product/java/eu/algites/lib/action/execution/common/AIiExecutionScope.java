package eu.algites.lib.action.execution.common;

/**
 * Hierarchical namespace and ownership boundary in an immutable execution plan.
 * A scope can describe a transaction or a subtransaction, but neither an execution
 * dependency nor a parent/child relation by itself changes top-level transaction ownership.
 */
public interface AIiExecutionScope extends AIiExecutionIdentifiable {

    /** Parent scope, or null for a root scope. */
    AIiExecutionScope getParentScope();

    /** Root scope whose identity is stable for the duration of execution. */
    AIiExecutionScope getRootScope();
}
