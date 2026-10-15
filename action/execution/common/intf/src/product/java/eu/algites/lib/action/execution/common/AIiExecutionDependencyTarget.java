package eu.algites.lib.action.execution.common;

/**
 * An action, scope or transaction whose observable outcome can satisfy a dependency.
 * Execution completion and final transaction outcome are different observation phases.
 */
public interface AIiExecutionDependencyTarget extends AIiExecutionIdentifiable {

    /** Stable kind identifier, e.g. ACTION, SUBTRANSACTION or TRANSACTION. */
    String getTargetKind();
}
