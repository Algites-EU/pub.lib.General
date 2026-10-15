package eu.algites.lib.journal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe, in-memory append-only journal.
 * A write lock protects before-listener validation and atomic batch publication.
 * After-listener notifications are queued in commit order and dispatched outside
 * the write lock, including when a listener recursively appends another batch.
 * Entries themselves must be immutable, as required by AIiJournalEntry.
 */
public final class AIcInMemoryJournal<E extends AIiJournalEntry, X extends Exception>
        implements AIiJournal<E, X> {
    private static final Logger LOGGER = Logger.getLogger(AIcInMemoryJournal.class.getName());

    private final ReentrantLock writeLock = new ReentrantLock();
    private final ArrayList<AIiJournalBatch<E>> batches = new ArrayList<>();
    private final CopyOnWriteArrayList<AIiJournalListener<E, X>> listeners = new CopyOnWriteArrayList<>();
    private final Object deliveryLock = new Object();
    private final Queue<Runnable> deliveryQueue = new ArrayDeque<>();
    private boolean delivering;

    @Override
    public AIiJournalSnapshot<E> snapshot() {
        writeLock.lock();
        try {
            return new AIcImmutableJournalSnapshot<>(batches);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<? extends AIiJournalBatch<E>> getBatches() {
        return snapshot().getBatches();
    }

    @Override
    public List<? extends E> getEntries() {
        return snapshot().getEntries();
    }

    @Override
    @SafeVarargs
    public final AIiJournalAppendResult<E> append(E... aEntries) throws X {
        return appendList(Arrays.asList(Objects.requireNonNull(aEntries)));
    }

    @Override
    public AIiJournalAppendResult<E> append(Supplier<? extends E[]> aEntries) throws X {
        return append(Objects.requireNonNull(aEntries).get());
    }

    @Override
    public AIiJournalAppendResult<E> appendList(List<? extends E> aEntries) throws X {
        Objects.requireNonNull(aEntries);
        List<E> locEntries = List.copyOf(aEntries);
        if (locEntries.isEmpty()) {
            return unchangedResult();
        }
        AIiJournalBatch<E> locBatch = new AIcImmutableJournalBatch<>(locEntries);
        return appendInternal(List.of(locBatch), null);
    }

    @Override
    public AIiJournalAppendResult<E> appendList(
            Supplier<? extends List<? extends E>> aEntries) throws X {
        return appendList(Objects.requireNonNull(aEntries).get());
    }

    @Override
    @SafeVarargs
    public final AIiJournalAppendResult<E> appendSupplied(
            Supplier<? extends E>... aEntries) throws X {
        return appendSupplied(Arrays.asList(Objects.requireNonNull(aEntries)));
    }

    @Override
    public AIiJournalAppendResult<E> appendSupplied(
            List<? extends Supplier<? extends E>> aEntries) throws X {
        Objects.requireNonNull(aEntries);
        List<? extends Supplier<? extends E>> locSuppliers = List.copyOf(aEntries);
        List<E> locEntries = new ArrayList<>(locSuppliers.size());
        for (Supplier<? extends E> locSupplier : locSuppliers) {
            locEntries.add(Objects.requireNonNull(locSupplier.get(), "Supplied entry must not be null"));
        }
        return appendList(locEntries);
    }

    @Override
    public AIiJournalAppendResult<E> appendJournal(AIiJournalSnapshot<? extends E> aSource) throws X {
        Objects.requireNonNull(aSource);
        List<AIiJournalBatch<E>> locBatches = new ArrayList<>();
        for (AIiJournalBatch<? extends E> locSourceBatch : aSource.getBatches()) {
            locBatches.add(new AIcImmutableJournalBatch<>(locSourceBatch.getEntries()));
        }
        if (locBatches.isEmpty()) {
            return unchangedResult();
        }
        AIiJournalSnapshot<? extends E> locSource =
                new AIcImmutableJournalSnapshot<>(locBatches);
        return appendInternal(locBatches, locSource);
    }

    @Override
    public AIiJournalAppendResult<E> appendJournal(
            Supplier<? extends AIiJournalSnapshot<? extends E>> aSource) throws X {
        return appendJournal(Objects.requireNonNull(aSource).get());
    }

    @Override
    public AIiJournalSubscription addListener(AIiJournalListener<E, X> aListener) {
        AIiJournalListener<E, X> locListener = Objects.requireNonNull(aListener);
        listeners.add(locListener);
        return () -> listeners.remove(locListener);
    }

    private AIiJournalAppendResult<E> unchangedResult() {
        return new AIcJournalAppendResult<>(List.of(), snapshot());
    }

    private AIiJournalAppendResult<E> appendInternal(List<AIiJournalBatch<E>> aBatches,
            AIiJournalSnapshot<? extends E> aSource) throws X {
        if (writeLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Appending to the same journal inside a before-append listener is forbidden");
        }
        AIiJournalAppendResult<E> locResult;
        writeLock.lock();
        try {
            AIiJournalSnapshot<E> locBefore = new AIcImmutableJournalSnapshot<>(batches);
            List<AIiJournalListener<E, X>> locListeners = List.copyOf(listeners);
            if (aSource == null) {
                for (AIiJournalListener<E, X> locListener : locListeners) {
                    locListener.beforeBatchAppend(locBefore, aBatches.get(0));
                }
            } else {
                for (AIiJournalListener<E, X> locListener : locListeners) {
                    locListener.beforeJournalAppend(locBefore, aSource);
                }
            }
            batches.addAll(aBatches);
            AIiJournalSnapshot<E> locAfter = new AIcImmutableJournalSnapshot<>(batches);
            locResult = new AIcJournalAppendResult<>(aBatches, locAfter);
            synchronized (deliveryLock) {
                deliveryQueue.add(() -> notifyAfter(locListeners, locAfter, aBatches, aSource));
            }
        } finally {
            writeLock.unlock();
        }
        drainNotifications();
        return locResult;
    }

    private void notifyAfter(List<AIiJournalListener<E, X>> aListeners,
            AIiJournalSnapshot<E> aAfter, List<AIiJournalBatch<E>> aBatches,
            AIiJournalSnapshot<? extends E> aSource) {
        for (AIiJournalListener<E, X> locListener : aListeners) {
            try {
                if (aSource == null) {
                    locListener.afterBatchAppend(aAfter, aBatches.get(0));
                } else {
                    locListener.afterJournalAppend(aAfter, aSource);
                }
            } catch (RuntimeException aFailure) {
                LOGGER.log(Level.SEVERE, "Journal after-append listener failed", aFailure);
            }
        }
    }

    private void drainNotifications() {
        synchronized (deliveryLock) {
            if (delivering) {
                return;
            }
            delivering = true;
        }
        try {
            for (;;) {
                Runnable locNotification;
                synchronized (deliveryLock) {
                    locNotification = deliveryQueue.poll();
                    if (locNotification == null) {
                        delivering = false;
                        return;
                    }
                }
                locNotification.run();
            }
        } finally {
            synchronized (deliveryLock) {
                delivering = false;
            }
        }
    }
}
