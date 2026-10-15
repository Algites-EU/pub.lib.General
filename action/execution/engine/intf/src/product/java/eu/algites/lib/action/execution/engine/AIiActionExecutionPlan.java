package eu.algites.lib.action.execution.engine;

/**
 * Plan definition, not worker scheduler and not a transaction journal.
 * The plan is fully configured before execute and becomes immutable on freeze.
 * Dependencies and actions are never appended to a running plan.
 */
public interface AIiActionExecutionPlan {

    /** Stable definition identity. */
    String getPlanId();

    /** All statically declared root execution scopes. */
    java.util.List<? extends AIiActionSchedulingScope> getRootScopes();

    /** True after structural validation and freeze. */
    boolean isFrozen();

    /** Validates ownership, dependencies, cycles and freezes the complete graph. */
    void freeze();
}
