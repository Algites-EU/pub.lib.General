package eu.algites.lib.action.execution.engine;

/**
 * Read-only snapshot of prerequisite results, scoped to one scheduled action.
 */
public interface AIiActionInputResolutionContext {

    /** Immutable observations keyed by stable dependency target identity. */
    java.util.Map<String, ? extends eu.algites.lib.action.execution.common.AIiExecutionTargetResult>
            getDependencyResults();
}
