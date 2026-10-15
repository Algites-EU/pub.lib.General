package eu.algites.lib.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

/** TestNG regression coverage for atomic journal append and snapshot semantics. */
public final class AItcInMemoryJournalTest {
    private static final class AIcTestEntry implements AIiJournalEntry {
        private final int value;

        AIcTestEntry(int aValue) {
            value = aValue;
        }

        int value() {
            return value;
        }
    }

    private static void verify(boolean aCondition, String aMessage) {
        if (!aCondition) {
            throw new AssertionError(aMessage);
        }
    }

    private static List<Integer> values(AIiReadOnlyJournal<AIcTestEntry> aJournal) {
        return aJournal.getEntries().stream().map(AIcTestEntry::value).toList();
    }

    @Test
    public void testFrozenSnapshotsAndReadOnlyInterface() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        locJournal.append(new AIcTestEntry(1), new AIcTestEntry(2));
        AIiReadOnlyJournal<AIcTestEntry> locReadOnly = locJournal;
        AIiJournalSnapshot<AIcTestEntry> locFrozen = locReadOnly.snapshot();
        verify(locFrozen.snapshot() == locFrozen, "snapshot of snapshot must return itself");
        locJournal.append(new AIcTestEntry(3));
        verify(values(locReadOnly).equals(List.of(1, 2, 3)), "live read-only view must change");
        verify(values(locFrozen).equals(List.of(1, 2)), "snapshot must remain frozen");
        verify(locFrozen.getBatches().size() == 1, "snapshot batch count must not change");
        boolean locRejected = false;
        try {
            locFrozen.getEntries().clear();
        } catch (UnsupportedOperationException aExpected) {
            locRejected = true;
        }
        verify(locRejected, "snapshot lists must not be modifiable");
    }

    @Test
    public void testPreservesSourceBatchesAndJournalOnlyCallbacks() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locSource = new AIcInMemoryJournal<>();
        locSource.append(new AIcTestEntry(1), new AIcTestEntry(2));
        locSource.append(new AIcTestEntry(3));
        AIcInMemoryJournal<AIcTestEntry, Exception> locTarget = new AIcInMemoryJournal<>();
        List<String> locEvents = new ArrayList<>();
        locTarget.addListener(new AIiJournalListener<>() {
            @Override
            public void beforeBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                locEvents.add("wrong-before-batch");
            }

            @Override
            public void beforeJournalAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalSnapshot<? extends AIcTestEntry> aSource) {
                verify(aTarget.getEntries().isEmpty(), "before must see original target");
                verify(aSource.getBatches().size() == 2, "source batches must be preserved");
                locEvents.add("before-journal");
            }

            @Override
            public void afterBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                locEvents.add("wrong-after-batch");
            }

            @Override
            public void afterJournalAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalSnapshot<? extends AIcTestEntry> aSource) {
                verify(aTarget.getBatches().size() == 2, "after must see complete target");
                locEvents.add("after-journal");
            }
        });
        AIiJournalAppendResult<AIcTestEntry> locResult = locTarget.appendJournal(locSource.snapshot());
        verify(locResult.getAppendedBatches().size() == 2, "two appended batches");
        verify(locTarget.getBatches().get(0).getEntries().size() == 2, "first boundary");
        verify(locTarget.getBatches().get(1).getEntries().size() == 1, "second boundary");
        verify(locEvents.equals(List.of("before-journal", "after-journal")), "wrong callback family");
    }

    @Test
    public void testBeforeVetoIsAtomic() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locSource = new AIcInMemoryJournal<>();
        locSource.append(new AIcTestEntry(10));
        locSource.append(new AIcTestEntry(20));
        AIcInMemoryJournal<AIcTestEntry, Exception> locTarget = new AIcInMemoryJournal<>();
        AtomicInteger locAfter = new AtomicInteger();
        locTarget.addListener(new AIiJournalListener<>() {
            @Override
            public void beforeJournalAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalSnapshot<? extends AIcTestEntry> aSource) throws Exception {
                throw new Exception("Rejected whole journal append");
            }
            @Override
            public void afterJournalAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalSnapshot<? extends AIcTestEntry> aSource) {
                locAfter.incrementAndGet();
            }
        });
        boolean locFailed = false;
        try {
            locTarget.appendJournal(locSource.snapshot());
        } catch (Exception aExpected) {
            locFailed = true;
        }
        verify(locFailed, "before listener exception must propagate");
        verify(locTarget.getBatches().isEmpty(), "none of the batches may be committed");
        verify(locAfter.get() == 0, "after listener must not fire after veto");
    }

    @Test
    public void testSuppliersAndSingleBatchListeners() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        List<String> locEvents = new ArrayList<>();
        locJournal.addListener(new AIiJournalListener<>() {
            @Override
            public void beforeBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                locEvents.add("before:" + aBatch.getEntries().size());
            }
            @Override
            public void afterBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                locEvents.add("after:" + aBatch.getEntries().size());
            }
        });
        locJournal.append(() -> new AIcTestEntry[]{new AIcTestEntry(1)});
        locJournal.appendList(() -> List.of(new AIcTestEntry(2), new AIcTestEntry(3)));
        locJournal.appendSupplied(() -> new AIcTestEntry(4), () -> new AIcTestEntry(5));
        locJournal.appendSupplied(List.of(() -> new AIcTestEntry(6)));
        verify(values(locJournal).equals(List.of(1, 2, 3, 4, 5, 6)), "supplier evaluation order");
        verify(locEvents.equals(List.of("before:1", "after:1", "before:2", "after:2",
                "before:2", "after:2", "before:1", "after:1")), "batch callback order");
        int locCount = locJournal.getBatches().size();
        locJournal.append(new AIcTestEntry[0]);
        locJournal.appendList(List.of());
        locJournal.appendJournal(new AIcInMemoryJournal<AIcTestEntry, Exception>().snapshot());
        verify(locJournal.getBatches().size() == locCount, "empty append is a no-op");
    }

    @Test
    public void testAfterListenerCanAppendWithoutReentrancy() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        List<Integer> locObserved = new ArrayList<>();
        locJournal.addListener(new AIiJournalListener<>() {
            @Override
            public void afterBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                int locValue = aBatch.getEntries().get(0).value();
                locObserved.add(locValue);
                if (locValue == 1) {
                    try {
                        locJournal.append(new AIcTestEntry(2));
                    } catch (Exception aFailure) {
                        throw new AssertionError(aFailure);
                    }
                }
            }
        });
        locJournal.append(new AIcTestEntry(1));
        verify(values(locJournal).equals(List.of(1, 2)), "nested append must be committed");
        verify(locObserved.equals(List.of(1, 2)), "after listener must preserve order");
    }

    @Test
    public void testConcurrentAppendProducesCompleteBatches() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        int locThreads = 6;
        int locIterations = 60;
        ExecutorService locPool = Executors.newFixedThreadPool(locThreads);
        CountDownLatch locReady = new CountDownLatch(locThreads);
        CountDownLatch locStart = new CountDownLatch(1);
        try {
            List<Future<?>> locTasks = new ArrayList<>();
            for (int locWorker = 0; locWorker < locThreads; locWorker++) {
                final int locWorkerId = locWorker;
                locTasks.add(locPool.submit(() -> {
                    locReady.countDown();
                    try {
                        locStart.await();
                        for (int locIndex = 0; locIndex < locIterations; locIndex++) {
                            locJournal.append(new AIcTestEntry(locWorkerId * 1000 + locIndex),
                                    new AIcTestEntry(locWorkerId * 1000 + locIndex));
                            AIiJournalSnapshot<AIcTestEntry> locSnapshot = locJournal.snapshot();
                            verify(locSnapshot.getEntries().size() == locSnapshot.getBatches().size() * 2,
                                    "snapshot must not see half a batch");
                        }
                    } catch (Exception aFailure) {
                        throw new AssertionError(aFailure);
                    }
                }));
            }
            verify(locReady.await(10, TimeUnit.SECONDS), "worker readiness");
            locStart.countDown();
            for (Future<?> locTask : locTasks) {
                locTask.get(30, TimeUnit.SECONDS);
            }
        } finally {
            locPool.shutdownNow();
        }
        verify(locJournal.getBatches().size() == locThreads * locIterations, "batch count");
        verify(locJournal.getEntries().size() == 2 * locThreads * locIterations, "entry count");
    }
    @Test
    public void testBatchVetoAndListenerDeregistration() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        AtomicInteger locSeen = new AtomicInteger();
        AIiJournalSubscription locSubscription = locJournal.addListener(new AIiJournalListener<>() {
            @Override
            public void beforeBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) throws Exception {
                locSeen.incrementAndGet();
                if (aBatch.getEntries().get(0).value() == 0) {
                    throw new Exception("Rejected batch");
                }
            }
        });
        boolean locFailed = false;
        try {
            locJournal.append(new AIcTestEntry(0));
        } catch (Exception aExpected) {
            locFailed = true;
        }
        verify(locFailed && locJournal.getEntries().isEmpty(), "batch veto must prevent any write");
        locSubscription.close();
        locJournal.append(new AIcTestEntry(0));
        verify(locSeen.get() == 1, "closed listener must not receive subsequent callbacks");
    }

    @Test
    public void testSupplierFailureHasNoSideEffects() throws Exception {
        AIcInMemoryJournal<AIcTestEntry, Exception> locJournal = new AIcInMemoryJournal<>();
        AtomicInteger locEvents = new AtomicInteger();
        locJournal.addListener(new AIiJournalListener<>() {
            @Override
            public void beforeBatchAppend(AIiJournalSnapshot<AIcTestEntry> aTarget,
                    AIiJournalBatch<AIcTestEntry> aBatch) {
                locEvents.incrementAndGet();
            }
        });
        boolean locFailed = false;
        try {
            locJournal.appendSupplied(List.of(
                    () -> new AIcTestEntry(1),
                    () -> { throw new IllegalStateException("Supplier failed"); }));
        } catch (IllegalStateException aExpected) {
            locFailed = true;
        }
        verify(locFailed, "supplier failure must propagate");
        verify(locEvents.get() == 0 && locJournal.getEntries().isEmpty(),
                "supplier failure must not produce append or callback");
    }

}
