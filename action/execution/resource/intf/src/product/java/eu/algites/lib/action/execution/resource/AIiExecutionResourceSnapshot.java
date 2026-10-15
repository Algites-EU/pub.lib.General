package eu.algites.lib.action.execution.resource;

/**
 * Immutable observation of usage in the entire engine across all concurrent plans.
 */
public interface AIiExecutionResourceSnapshot {

    /** Identity of the monitored resource. */
    String getResourceId();

    /** Configured capacity at the instant of the snapshot. */
    int getMaxConcurrentUsages();

    /** Number of currently reserved usages at that instant. */
    int getReservedUsages();
}
