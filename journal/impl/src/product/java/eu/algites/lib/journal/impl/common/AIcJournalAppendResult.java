package eu.algites.lib.journal.impl.common;


import eu.algites.lib.journal.AIiJournalAppendResult;
import eu.algites.lib.journal.AIiJournalBatch;
import eu.algites.lib.journal.AIiJournalEntry;
import eu.algites.lib.journal.AIiJournalSnapshot;
import java.util.List;
import java.util.Objects;

/** Immutable append result. */
final class AIcJournalAppendResult<E extends AIiJournalEntry>
        implements AIiJournalAppendResult<E> {
    private final List<AIiJournalBatch<E>> appendedBatches;
    private final AIiJournalSnapshot<E> journalSnapshot;

    AIcJournalAppendResult(List<? extends AIiJournalBatch<E>> aBatches,
            AIiJournalSnapshot<E> aSnapshot) {
        appendedBatches = List.copyOf(aBatches);
        journalSnapshot = Objects.requireNonNull(aSnapshot);
    }

    @Override
    public List<AIiJournalBatch<E>> getAppendedBatches() {
        return appendedBatches;
    }

    @Override
    public AIiJournalSnapshot<E> getJournalSnapshot() {
        return journalSnapshot;
    }
}
