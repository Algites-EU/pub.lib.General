package eu.algites.lib.journal;

/**
 * Immutable journal view. Both batches and entries remain unchanged for its lifetime.
 * Implementations must return this instance (or an equivalent frozen view) from snapshot().
 */
public interface AIiJournalSnapshot<E extends AIiJournalEntry>
        extends AIiReadOnlyJournal<E> {
}
