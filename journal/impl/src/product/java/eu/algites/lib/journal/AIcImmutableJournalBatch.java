package eu.algites.lib.journal;

import java.util.List;

/** Immutable journal batch implementation. */
final class AIcImmutableJournalBatch<E extends AIiJournalEntry> implements AIiJournalBatch<E> {
    private final List<E> entries;

    AIcImmutableJournalBatch(List<? extends E> aEntries) {
        if (aEntries.isEmpty()) {
            throw new IllegalArgumentException("Journal batch must not be empty");
        }
        entries = List.copyOf(aEntries);
    }

    @Override
    public List<E> getEntries() {
        return entries;
    }
}
