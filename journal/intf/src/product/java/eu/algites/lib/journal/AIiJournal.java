package eu.algites.lib.journal;

import java.util.List;
import java.util.function.Supplier;

/**
 * Thread-safe append-only journal. Each operation is atomic and linearizable.
 * Suppliers run once in input order before append validation. Null entries are forbidden.
 * After-append listeners are notified in commit order, outside the journal write lock;
 * they may run on another caller's thread and never change append success.
 */
public interface AIiJournal<E extends AIiJournalEntry, X extends Exception>
        extends AIiReadOnlyJournal<E> {

    /** Atomically appends one batch. A zero-length batch is a no-op. */
    AIiJournalAppendResult<E> append(E... aEntries) throws X;

    /** Supplies an array and appends it as one batch. */
    AIiJournalAppendResult<E> append(Supplier<? extends E[]> aEntries) throws X;

    /** Atomically appends the supplied list as one batch. */
    AIiJournalAppendResult<E> appendList(List<? extends E> aEntries) throws X;

    /** Supplies a list and appends it as one batch. */
    AIiJournalAppendResult<E> appendList(
            Supplier<? extends List<? extends E>> aEntries) throws X;

    /** Evaluates one supplier per entry in order, then appends a single batch. */
    AIiJournalAppendResult<E> appendSupplied(Supplier<? extends E>... aEntries) throws X;

    /** Evaluates one supplier per entry in list order, then appends a single batch. */
    AIiJournalAppendResult<E> appendSupplied(
            List<? extends Supplier<? extends E>> aEntries) throws X;

    /** Atomically appends all source batches, preserving each original batch boundary. */
    AIiJournalAppendResult<E> appendJournal(AIiJournalSnapshot<? extends E> aSource) throws X;

    /** Obtains a source snapshot and appends its batches atomically. */
    AIiJournalAppendResult<E> appendJournal(
            Supplier<? extends AIiJournalSnapshot<? extends E>> aSource) throws X;

    /** Adds a listener for both operation kinds. Thread-safe. */
    AIiJournalSubscription addListener(AIiJournalListener<E, X> aListener);
}
