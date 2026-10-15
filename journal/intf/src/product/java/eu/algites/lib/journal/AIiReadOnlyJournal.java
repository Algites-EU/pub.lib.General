package eu.algites.lib.journal;

import java.util.List;

/** Readable journal, which may still change through another reference. */
public interface AIiReadOnlyJournal<E extends AIiJournalEntry> {
    /** Returns an unmodifiable point-in-time batch list. */
    List<? extends AIiJournalBatch<E>> getBatches();

    /** Returns an unmodifiable point-in-time flattened entry list. */
    List<? extends E> getEntries();

    /** Atomically obtains an immutable, internally consistent journal view. */
    AIiJournalSnapshot<E> snapshot();
}
