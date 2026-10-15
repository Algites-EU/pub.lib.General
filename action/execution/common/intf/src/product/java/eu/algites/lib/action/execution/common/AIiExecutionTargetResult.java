package eu.algites.lib.action.execution.common;

/**
 * Immutable observation of an action or scope at a particular lifecycle boundary.
 * Do not interpret action SUCCEEDED as a durable commitment of external effects.
 */
public interface AIiExecutionTargetResult extends AIiExecutionIdentifiable {

    /** Target kind compatible with the dependency-target definition. */
    String getTargetKind();

    /** Stable lifecycle state identifier; documented by its domain implementation. */
    String getOutcome();

    /** True when the observed lifecycle boundary is definitive for this result. */
    boolean isTerminal();

    /** Structured failure details, or null where no failure exists. */
    AIiExecutionFailure getFailure();
}
