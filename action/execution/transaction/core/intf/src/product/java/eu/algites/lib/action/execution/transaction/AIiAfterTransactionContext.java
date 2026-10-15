package eu.algites.lib.action.execution.transaction;

/**
 * Independent context for final-result actions; it MUST NOT own parent rollback.
 * An after action may execute a separately defined independent new plan, but
 * may never insert nodes into the already executing/finalized original plan.
 */
public interface AIiAfterTransactionContext extends eu.algites.lib.action.execution.common.AIiExecutionContext {

    /** Stable id of the completed source transaction whose outcome caused the reaction. */
    String getSourceTransactionId();
}
