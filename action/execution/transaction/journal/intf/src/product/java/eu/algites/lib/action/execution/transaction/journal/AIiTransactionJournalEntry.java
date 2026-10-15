package eu.algites.lib.action.execution.transaction.journal;

/**
 * Serializable event representing an intended or observed transaction lifecycle change.
 * An intent MUST be durably appended before the corresponding external effect.
 * Its payload must provide enough stable identifiers for restart recovery.
 */
public interface AIiTransactionJournalEntry extends eu.algites.lib.journal.AIiJournalEntry {

    /** Owning top-level transaction identity. */
    String getTransactionId();

    /** Event type, such as ACTION_INTENT, ACTION_RESULT, COMMIT or ROLLBACK. */
    String getEventKind();

    /** Related action identity or null for transaction-wide events. */
    String getActionId();

    /** Journaled executor resolver identity where an action is involved. */
    String getExecutorId();
}
