package eu.algites.lib.action.execution.common;

/**
 * Immutable declaration linking a dependent node to an observable target condition.
 * Relationships influence scheduling but do not transfer transaction membership.
 */
public interface AIiExecutionDependency extends AIiExecutionIdentifiable {

    /** Target action, subtransaction or top-level transaction. */
    AIiExecutionDependencyTarget getTarget();

    /** Predicate evaluated against snapshots of the target result. */
    AIiExecutionDependencyCondition getCondition();
}
