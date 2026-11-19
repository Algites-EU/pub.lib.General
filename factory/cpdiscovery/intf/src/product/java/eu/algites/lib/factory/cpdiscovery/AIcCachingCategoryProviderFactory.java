package eu.algites.lib.factory.cpdiscovery;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Optional reusable base for factories whose instances are safe to share by category and context.
 * Each factory object owns an independent cache. Both category and context form the key; a null
 * context is supported. Context objects must have stable {@code equals} and {@code hashCode}
 * implementations for as long as they are used as keys. Alternatively, callers may use immutable
 * identity-based contexts. Factory implementations must not cache mutable per-invocation state.
 *
 * <p>Only successful, non-null creations are cached. Concurrent callers for the same key share
 * one creation via {@link ConcurrentHashMap#computeIfAbsent}. The generic registry does not cache
 * automatically: factories with stateful or short-lived results should implement the SPI directly.
 *
 * @param <I> produced provider-interface type
 * @param <C> factory resolution-context type
 */
public abstract class AIcCachingCategoryProviderFactory<I, C>
        implements AIiCategoryProviderFactory<I, C> {
    private final ConcurrentMap<AIcdCacheKey, I> cache = new ConcurrentHashMap<>();

    /**
     * Constructs a per-factory instance cache; subclasses are responsible for the SPI type metadata.
     */
    protected AIcCachingCategoryProviderFactory() {
    }

    /**
     * Returns the first successfully created instance for the given category and context.
     * A failed creation is not cached, and a null result is rejected to avoid ambiguous misses.
     * Recursive creation for the same key is not supported.
     *
     * @param aCategory nonblank category identifier
     * @param aContext immutable or identity-stable context, potentially null
     * @return cached instance, never null
     * @throws IllegalArgumentException if the category is blank
     * @throws NullPointerException if creation returns null
     */
    @Override
    public final I createInstance(String aCategory, C aContext) {
        Objects.requireNonNull(aCategory, "category");
        if (aCategory.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }
        return cache.computeIfAbsent(new AIcdCacheKey(aCategory, aContext),
                locKey -> Objects.requireNonNull(
                        createUncachedInstance(aCategory, aContext), "Factory returned null for " + aCategory));
    }

    /**
     * Creates one shareable implementation object on a cache miss. This operation must be safe
     * under concurrency for distinct keys and must not recursively request its own cache key.
     *
     * @param aCategory requested category
     * @param aContext supplied context, potentially null
     * @return non-null instance to retain for the lifetime of this factory, unless invalidated
     */
    protected abstract I createUncachedInstance(String aCategory, C aContext);

    /**
     * Invalidates all previously cached instances owned by this factory. Already returned
     * references remain valid, and an in-flight creation may complete concurrently.
     * Use only when the factory's external inputs have changed and consumers can tolerate refresh.
     */
    protected final void clearInstanceCache() {
        cache.clear();
    }

    /** Cache key using value equality for a stable context, including a null context. */
    private record AIcdCacheKey(String category, Object context) {
    }
}
