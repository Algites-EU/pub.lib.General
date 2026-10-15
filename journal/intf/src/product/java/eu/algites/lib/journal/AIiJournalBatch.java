package eu.algites.lib.journal;

import java.util.List;

/** One nonempty, ordered atomic group of entries. */
public interface AIiJournalBatch<E extends AIiJournalEntry> {
    /** Returns an unmodifiable, ordered list of immutable entries. */
    List<? extends E> getEntries();
}
