package eu.algites.lib.action.execution.transaction;

/**
 * Child scope nested in a root transaction. It is NOT a second top-level transaction.
 * The child may execute and report success, but its effects can still be rolled
 * back if another scope causes root transaction failure.
 */
public interface AIiExecutionSubtransaction extends AIiTransactionScope, eu.algites.lib.action.execution.common.AIiExecutionDependencyTarget {

    /** Immediate parent scope; its root transaction has identical rollback ownership. */
    AIiTransactionScope getParentTransactionScope();
}
