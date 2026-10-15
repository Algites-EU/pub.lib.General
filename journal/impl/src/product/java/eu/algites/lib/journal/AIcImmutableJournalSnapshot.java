package eu.algites.lib.journal;

import java.util.ArrayList;
import java.util.List;

/** Immutable snapshot implementation with two synchronized read projections. */
final class AIcImmutableJournalSnapshot<E extends AIiJournalEntry>
        implements AIiJournalSnapshot<E> {
    private final List<AIiJournalBatch<E>> batches;
    private final List<E> entries;

    AIcImmutableJournalSnapshot(List<? extends AIiJournalBatch<E>> aBatches) {
        batches = List.copyOf(aBatches);
        List<E> locEntries = new ArrayList<>();
        for (AIiJournalBatch<E> locBatch : batches) {
            locEntries.addAll(locBatch.getEntries());
        }
        entries = List.copyOf(locEntries);
    }

    @Override
    public List<AIiJournalBatch<E>> getBatches() {
        return batches;
    }

    @Override
    public List<E> getEntries() {
        return entries;
    }

    @Override
    public AIiJournalSnapshot<E> snapshot() {
        return this;
    }
}
