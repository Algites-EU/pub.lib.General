package eu.algites.lib.common.list;

import eu.algites.lib.common.interval.AIiInterval;
import eu.algites.lib.common.interval.AInIntervalBoundary;
import eu.algites.lib.common.interval.AIsIntervalUtils;
import eu.algites.lib.common.validation.AIiValidatable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Mutable list with nullable minimum and maximum cardinality constraints.
 * A null bound means no constraint on that side. Minimum cardinality affects
 * validity, not initial construction. Mutation policy applies to both bounds.
 *
 * <p>{@link #modifyAtomically(Consumer)} validates the final size of a working
 * copy and publishes the change only on successful completion. This provides
 * all-or-nothing mutation semantics, not cross-thread synchronization. No
 * callback should modify this list directly while modifying its working copy.</p>
 *
 * @param <T> element type
 */
public class AIcBoundedList<T> extends AbstractList<T> implements RandomAccess, AIiValidatable {
    private final List<T> values = new ArrayList<>();
    private final Integer minSize;
    private final Integer maxSize;
    private final AInBoundsMutationPolicy mutationPolicy;

    /** Constructs an unrestricted-size list with the default mutation policy. */
    public AIcBoundedList() { this((Integer) null, (Integer) null); }

    /** Constructs a list enforcing non-worsening size changes. */
    public AIcBoundedList(Integer aMinSize, Integer aMaxSize) {
        this(aMinSize, aMaxSize, AInBoundsMutationPolicy.PREVENT_WORSENING);
    }

    /** Constructs a list with explicit size bounds and mutation policy. */
    public AIcBoundedList(Integer aMinSize, Integer aMaxSize, AInBoundsMutationPolicy aPolicy) {
        if (aMinSize != null && aMinSize < 0) throw new IllegalArgumentException("Minimum size cannot be negative");
        if (aMaxSize != null && aMaxSize < 0) throw new IllegalArgumentException("Maximum size cannot be negative");
        if (aMinSize != null && aMaxSize != null && aMinSize > aMaxSize)
            throw new IllegalArgumentException("Minimum size exceeds maximum size");
        minSize = aMinSize;
        maxSize = aMaxSize;
        mutationPolicy = Objects.requireNonNull(aPolicy, "Mutation policy");
    }

    /** Constructs a list from an integer-valued interval. */
    public AIcBoundedList(AIiInterval<Integer> aInterval) {
        this(aInterval, AInBoundsMutationPolicy.PREVENT_WORSENING);
    }

    /** Constructs a list from an integer-valued interval with explicit policy. */
    public AIcBoundedList(AIiInterval<Integer> aInterval, AInBoundsMutationPolicy aPolicy) {
        this(intervalMin(aInterval), intervalMax(aInterval), aPolicy);
    }

    private static Integer intervalMin(AIiInterval<Integer> aInterval) {
        validateInterval(aInterval);
        if (aInterval.getLeftBoundary().isBoundaryValueIgnored()) return null;
        int locValue = aInterval.getLeftValue();
        if (!aInterval.getLeftBoundary().isOpen()) return locValue;
        if (locValue == Integer.MAX_VALUE) throw new IllegalArgumentException("Open lower bound exceeds integer sizes");
        return locValue + 1;
    }

    private static Integer intervalMax(AIiInterval<Integer> aInterval) {
        validateInterval(aInterval);
        if (aInterval.getRightBoundary().isBoundaryValueIgnored()) return null;
        int locValue = aInterval.getRightValue();
        if (!aInterval.getRightBoundary().isOpen()) return locValue;
        if (locValue == Integer.MIN_VALUE) throw new IllegalArgumentException("Open upper bound exceeds integer sizes");
        return locValue - 1;
    }

    private static void validateInterval(AIiInterval<Integer> aInterval) {
        Objects.requireNonNull(aInterval, "Size interval");
        AIsIntervalUtils.validateInterval(aInterval);
    }

    /** Returns the inclusive lower size bound or null. */
    public Integer getMinSize() { return minSize; }
    /** Returns the inclusive upper size bound or null. */
    public Integer getMaxSize() { return maxSize; }
    /** Returns the selected mutation policy. */
    public AInBoundsMutationPolicy getMutationPolicy() { return mutationPolicy; }

    @Override public int size() { return values.size(); }
    @Override public T get(int aIndex) { return values.get(aIndex); }

    /** Replaces an element; the list size is unchanged. */
    @Override public T set(int aIndex, T aElement) { return values.set(aIndex, aElement); }

    @Override public void add(int aIndex, T aElement) {
        mutate(locItems -> locItems.add(aIndex, aElement));
    }

    @Override public T remove(int aIndex) {
        List<T> locCandidate = new ArrayList<>(values);
        T locRemoved = locCandidate.remove(aIndex);
        publish(locCandidate, true);
        return locRemoved;
    }

    @Override public boolean addAll(Collection<? extends T> aItems) { return addAll(size(), aItems); }

    @Override public boolean addAll(int aIndex, Collection<? extends T> aItems) {
        Objects.requireNonNull(aItems, "Items");
        List<T> locCandidate = new ArrayList<>(values);
        boolean locChanged = locCandidate.addAll(aIndex, aItems);
        if (locChanged) publish(locCandidate, true);
        return locChanged;
    }

    /** Ensures that subList(...).clear() also observes all-or-nothing bounds. */
    @Override protected void removeRange(int aFromIndex, int aToIndex) {
        List<T> locCandidate = new ArrayList<>(values);
        locCandidate.subList(aFromIndex, aToIndex).clear();
        if (aFromIndex != aToIndex) publish(locCandidate, true);
    }

    @Override public boolean removeAll(Collection<?> aItems) {
        Objects.requireNonNull(aItems, "Items");
        return modifySelection(locItems -> locItems.removeAll(aItems));
    }

    @Override public boolean retainAll(Collection<?> aItems) {
        Objects.requireNonNull(aItems, "Items");
        return modifySelection(locItems -> locItems.retainAll(aItems));
    }

    @Override public boolean removeIf(Predicate<? super T> aFilter) {
        Objects.requireNonNull(aFilter, "Filter");
        return modifySelection(locItems -> locItems.removeIf(aFilter));
    }

    @Override public void clear() {
        if (!values.isEmpty()) publish(new ArrayList<>(), true);
    }

    private boolean modifySelection(java.util.function.Function<List<T>, Boolean> aChange) {
        List<T> locCandidate = new ArrayList<>(values);
        boolean locChanged = aChange.apply(locCandidate);
        if (locChanged) publish(locCandidate, true);
        return locChanged;
    }

    private void mutate(Consumer<List<T>> aChange) {
        List<T> locCandidate = new ArrayList<>(values);
        aChange.accept(locCandidate);
        publish(locCandidate, true);
    }

    /**
     * Applies a series of mutations to a detached working list. The candidate
     * is committed only if the callback succeeds and its final size satisfies
     * the selected mutation policy relative to the initial list size.
     * Intermediate candidate sizes are intentionally unrestricted.
     */
    public void modifyAtomically(Consumer<List<T>> aChange) {
        Objects.requireNonNull(aChange, "Change callback");
        List<T> locCandidate = new ArrayList<>(values);
        aChange.accept(locCandidate);
        publish(locCandidate, !locCandidate.equals(values));
    }

    private void publish(List<T> aCandidate, boolean aChanged) {
        if (mutationPolicy == AInBoundsMutationPolicy.PREVENT_WORSENING) {
            long locBefore = values.size(), locAfter = aCandidate.size();
            if (below(locAfter) > below(locBefore) || above(locAfter) > above(locBefore))
                throw new IllegalStateException("List mutation increases a cardinality constraint violation");
        }
        if (aChanged) {
            values.clear();
            values.addAll(aCandidate);
            modCount++;
        }
    }

    private long below(long aSize) { return minSize == null ? 0 : Math.max(0L, (long) minSize - aSize); }
    private long above(long aSize) { return maxSize == null ? 0 : Math.max(0L, aSize - maxSize); }

    @Override public boolean isValid() { return below(size()) == 0 && above(size()) == 0; }

    @Override public String getInvalidStateMessage(Locale aLocale) {
        if (isValid()) return "";
        boolean locCzech = aLocale != null && "cs".equals(aLocale.getLanguage());
        if (below(size()) > 0) return locCzech
                ? "Seznam musí obsahovat alespoň " + minSize + " položek; aktuálně obsahuje " + size() + "."
                : "List must contain at least " + minSize + " items; current size is " + size() + ".";
        return locCzech
                ? "Seznam smí obsahovat nejvýše " + maxSize + " položek; aktuálně obsahuje " + size() + "."
                : "List must contain at most " + maxSize + " items; current size is " + size() + ".";
    }
}
