package eu.algites.lib.factory.cpdiscovery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Standalone regression tests for opt-in CPDiscovery factory instance caching. */
public final class AItcCachingCategoryProviderFactoryTest {
    /** Prevents instantiation of the test entry point. */
    private AItcCachingCategoryProviderFactoryTest() {
    }

    /** Stable value object that is safe to use as a cache context. */
    private record AIcdContext(String value) implements AIiCategoryProviderResolutionContext {
    }

    /** Test factory counting distinct instance creations under contention. */
    private static final class AIcTestFactory extends AIcCachingCategoryProviderFactory<Object, AIiCategoryProviderResolutionContext> {
        private final AtomicInteger creations = new AtomicInteger();
        /** {@inheritDoc} */
        @Override public Class<Object> getImplementationInterfaceType() { return Object.class; }
        /** {@inheritDoc} */
        @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
            return AIiCategoryProviderResolutionContext.class;
        }
        /** {@inheritDoc} */
        @Override protected Object createUncachedInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
            creations.incrementAndGet();
            return new Object();
        }
    }

    /**
     * Verifies null contexts, category/context distinctions, value equality and once-only
     * concurrent creation without relying on an external test runner.
     * @param aArguments ignored
     * @throws InterruptedException if the concurrency test is interrupted
     */
    public static void main(String[] aArguments) throws InterruptedException {
        AIcTestFactory locFactory = new AIcTestFactory();
        Object locDefault = locFactory.createInstance("a", null);
        check(locDefault == locFactory.createInstance("a", null), "null-context cache miss");
        Object locContext = locFactory.createInstance("a", new AIcdContext("x"));
        check(locContext == locFactory.createInstance("a", new AIcdContext("x")), "equal contexts must share");
        check(locContext != locFactory.createInstance("a", new AIcdContext("y")), "distinct contexts merged");
        check(locDefault != locFactory.createInstance("b", null), "distinct categories merged");
        check(locFactory.creations.get() == 4, "unexpected cache size");

        AtomicInteger locErrors = new AtomicInteger();
        CountDownLatch locReady = new CountDownLatch(12);
        CountDownLatch locStart = new CountDownLatch(1);
        List<Thread> locThreads = new ArrayList<>();
        Object[] locOutputs = new Object[12];
        for (int index = 0; index < 12; index++) {
            int locIndex = index;
            Thread locThread = new Thread(() -> {
                locReady.countDown();
                try {
                    locStart.await();
                    locOutputs[locIndex] = locFactory.createInstance("shared", new AIcdContext("z"));
                } catch (Throwable aError) {
                    locErrors.incrementAndGet();
                }
            });
            locThreads.add(locThread);
            locThread.start();
        }
        locReady.await();
        locStart.countDown();
        for (Thread locThread : locThreads) locThread.join();
        check(locErrors.get() == 0, "thread failed");
        for (Object locOutput : locOutputs) check(locOutput == locOutputs[0], "concurrent cache mismatch");
        check(locFactory.creations.get() == 5, "duplicate concurrent creations");
        System.out.println("OK: caching factory: 5 unique keys, concurrent once-only creation");
    }

    private static void check(boolean aValue, String aMessage) {
        if (!aValue) throw new AssertionError(aMessage);
    }
}
