package eu.algites.lib.journal;

import java.util.List;

/** Immutable result of one successful journal append operation. */
public interface AIiJournalAppendResult<E extends AIiJournalEntry> {
    /** Batches appended by this operation, in source order. */
    List<? extends AIiJournalBatch<E>> getAppendedBatches();

    /** Frozen destination state immediately after the operation. */
    AIiJournalSnapshot<E> getJournalSnapshot();
}
