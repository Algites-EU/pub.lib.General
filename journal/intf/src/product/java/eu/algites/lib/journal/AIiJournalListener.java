package eu.algites.lib.journal;

/**
 * Listens to exactly the operation performed: batch append or journal append.
 * Before callbacks may veto the entire operation. After callbacks cannot undo a committed append.
 */
public interface AIiJournalListener<E extends AIiJournalEntry, X extends Exception> {
    /** Called before a single batch is committed, against the original target snapshot. */
    default void beforeBatchAppend(AIiJournalSnapshot<E> aTarget,
            AIiJournalBatch<E> aBatch) throws X {
    }

    /** Called before all source batches are atomically committed to the target. */
    default void beforeJournalAppend(AIiJournalSnapshot<E> aTarget,
            AIiJournalSnapshot<? extends E> aSource) throws X {
    }

    /** Called after a single batch has been committed. */
    default void afterBatchAppend(AIiJournalSnapshot<E> aTarget,
            AIiJournalBatch<E> aBatch) {
    }

    /** Called after all source batches have been committed atomically. */
    default void afterJournalAppend(AIiJournalSnapshot<E> aTarget,
            AIiJournalSnapshot<? extends E> aSource) {
    }
}
