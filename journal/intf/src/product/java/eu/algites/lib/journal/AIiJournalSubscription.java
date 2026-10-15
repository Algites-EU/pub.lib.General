package eu.algites.lib.journal;

/** Listener registration handle. Closing deregisters the listener. */
public interface AIiJournalSubscription extends AutoCloseable {
    @Override
    void close();
}
