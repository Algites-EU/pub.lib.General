package eu.algites.lib.action.execution.transaction.journal;

/**
 * Specialized durable append-only transaction journal with one logical stream per
 * top-level transaction. A child subtransaction uses its root journal; external
 * independent transactions have separate journals. The same generic before/after
 * append semantics apply; an in-memory implementation alone is not crash-durable.
 * @param <X> domain-specific append validation exception
 */
public interface AIiTransactionJournal<X extends Exception>
        extends eu.algites.lib.journal.AIiJournal<AIiTransactionJournalEntry, X> {
}
