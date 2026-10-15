package eu.algites.lib.action.execution.common;

/**
 * Pure deterministic predicate and termination test for a dependency.
 * A false predicate is NOT sufficient to resolve a dependency until isDecidable
 * returns true. This prevents FAILED conditions from waiting forever after success.
 */
public interface AIiExecutionDependencyCondition {

    /** True if the already observed result triggers the dependent action. */
    boolean matches(AIiExecutionTargetResult aResult);

    /** True if later states can no longer change this decision. */
    boolean isDecidable(AIiExecutionTargetResult aResult);
}
