package eu.algites.lib.common.list;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * List-backed, multi-map collection with cached live key views and weakly tracked
 * membership sublists. The original {@code List} order is authoritative, whereas
 * map iteration order is deliberately unspecified. Each key maps to an immutable
 * snapshot list, including when the key is declared unique.
 *
 * <p>All mutations performed through the supported API validate all registered
 * indexes before publishing the new list and its indexes. Unique-key conflicts,
 * invalid keys and extractor failures leave the collection unchanged. Extractors
 * must be pure; their external side effects cannot be rolled back. No implicit
 * locking or thread safety is provided.</p>
 *
 * <p>The implementation keeps one entry token per occurrence, so tracked sublists
 * can distinguish duplicate value objects. Parent insertion does not change
 * their membership, and removal through the parent, any map or another sublist
 * removes the corresponding occurrence from every active membership view.
 * {@code subList} remains a backed view but intentionally has stronger semantics
 * than Java's usual positional-range view following parent modifications.</p>
 *
 * <p>Registered definitions cannot be changed after construction. In particular,
 * key properties on stored objects and extracted key objects must not be mutated
 * while inserted. Clients violating that contract may see stale keys or
 * undefined lookup behavior; the container does not observe external mutations.</p>
 *
 * @param <V> non-null type of stored values
 * @author linhart1
 */
public final class AIcMultiMappedList<V> extends AbstractList<V> implements AIiMultiMappedList<V> {
    private static final class Entry<V> {
        private final V value;
        private Entry(V value) { this.value = Objects.requireNonNull(value, "value"); }
    }

    private static final class Index<V> {
        private final AIiKeyDefinition<V, ?> definition;
        private Map<Object, List<Entry<V>>> groups = new HashMap<>();
        private Map<?, ?> view;
        private Index(AIiKeyDefinition<V, ?> definition) { this.definition = definition; }
    }

    private final List<Entry<V>> entries = new ArrayList<>();
    private final List<Index<V>> indexes = new ArrayList<>();
    private final IdentityHashMap<AIiKeyDefinition<V, ?>, Index<V>> byDefinition =
        new IdentityHashMap<>();
    private final ReferenceQueue<TrackedSubList> subListQueue = new ReferenceQueue<>();
    private final Set<WeakReference<TrackedSubList>> subLists = new HashSet<>();
    private long mutationVersion;

    /**
     * Constructs a list without key mappings. Additional definitions cannot be
     * registered after construction.
     */
    public AIcMultiMappedList() { this(List.of()); }

    /**
     * Constructs a list with a fixed set of independently addressable key maps.
     *
     * @param definitions non-null collection of non-null definitions; each must
     *                    have a non-blank identifier unique in this collection
     * @throws NullPointerException if the collection or a definition is null
     * @throws IllegalArgumentException if identifiers are blank or repeated, or
     *                                  the same definition instance is repeated
     */
    public AIcMultiMappedList(Collection<? extends AIiKeyDefinition<V, ?>> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        Set<String> ids = new HashSet<>();
        for (AIiKeyDefinition<V, ?> def : definitions) {
            Objects.requireNonNull(def, "definition");
            String id = Objects.requireNonNull(def.getId(), "definition id");
            if (id.isBlank() || !ids.add(id) || byDefinition.containsKey(def)) {
                throw new IllegalArgumentException("Duplicate or blank key definition id: " + id);
            }
            Index<V> index = new Index<>(def);
            indexes.add(index);
            byDefinition.put(def, index);
        }
    }

    /**
     * Constructs a list with the supplied definitions in declaration order.
     *
     * @param definitions non-null key definitions with unique non-blank identifiers
     * @throws NullPointerException if the array or one of its definitions is null
     * @throws IllegalArgumentException if a definition identifier is duplicated or blank
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public AIcMultiMappedList(AIiKeyDefinition<V, ?>... definitions) {
        this(List.of(Objects.requireNonNull(definitions, "definitions")));
    }

    /** {@inheritDoc} */
    @Override public int size() { return entries.size(); }

    /** {@inheritDoc} */
    @Override public V get(int index) { return entries.get(index).value; }

    /** {@inheritDoc} */
    @Override public void add(int index, V value) {
        insertAll(index, List.of(Objects.requireNonNull(value, "value")));
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(int index, Collection<? extends V> values) {
        Objects.requireNonNull(values, "values");
        List<V> copied = new ArrayList<>(values);
        if (index < 0 || index > size()) throw new IndexOutOfBoundsException(index);
        if (copied.isEmpty()) return false;
        insertAll(index, copied);
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(Collection<? extends V> values) { return addAll(size(), values); }

    private List<Entry<V>> insertAll(int index, Collection<? extends V> values) {
        List<Entry<V>> fresh = new ArrayList<>();
        for (V value : values) fresh.add(new Entry<>(value));
        List<Entry<V>> candidate = new ArrayList<>(entries);
        candidate.addAll(index, fresh);
        publish(candidate, Map.of(), true);
        return fresh;
    }

    /** {@inheritDoc} */
    @Override public V set(int index, V value) {
        Entry<V> old = entries.get(index);
        replaceEntry(old, value);
        return old.value;
    }

    private void replaceEntry(Entry<V> old, V value) {
        Entry<V> replacement = new Entry<>(value);
        List<Entry<V>> candidate = new ArrayList<>(entries);
        int index = position(old);
        candidate.set(index, replacement);
        IdentityHashMap<Entry<V>, Entry<V>> replaced = new IdentityHashMap<>();
        replaced.put(old, replacement);
        publish(candidate, replaced, false);
    }

    /** {@inheritDoc} */
    @Override public V remove(int index) {
        Entry<V> old = entries.get(index);
        removeEntry(old);
        return old.value;
    }

    private void removeEntry(Entry<V> old) {
        List<Entry<V>> candidate = new ArrayList<>(entries);
        candidate.remove(position(old));
        publish(candidate, Map.of(), true);
    }

    private int position(Entry<V> searched) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == searched) return i;
        }
        throw new IllegalStateException("Entry no longer belongs to the source list");
    }

    /** {@inheritDoc} */
    @Override protected void removeRange(int fromIndex, int toIndex) {
        List<Entry<V>> candidate = new ArrayList<>(entries);
        candidate.subList(fromIndex, toIndex).clear();
        if (fromIndex != toIndex) publish(candidate, Map.of(), true);
    }

    /** {@inheritDoc} */
    @Override public void clear() {
        if (!entries.isEmpty()) publish(List.of(), Map.of(), true);
    }

    /**
     * Removes all occurrences satisfying the predicate as one atomic mutation.
     *
     * @param predicate non-null condition evaluated against each stored value
     * @return true if any matching occurrences were removed
     * @throws NullPointerException if predicate is null
     * @throws RuntimeException if predicate throws; the list remains unchanged
     */
    @Override public boolean removeIf(Predicate<? super V> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        List<Entry<V>> candidate = new ArrayList<>();
        for (Entry<V> entry : entries) {
            if (!predicate.test(entry.value)) candidate.add(entry);
        }
        if (candidate.size() == entries.size()) return false;
        publish(candidate, Map.of(), true);
        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean removeAll(Collection<?> values) {
        Objects.requireNonNull(values, "values");
        return removeIf(values::contains);
    }

    /** {@inheritDoc} */
    @Override public boolean retainAll(Collection<?> values) {
        Objects.requireNonNull(values, "values");
        return removeIf(value -> !values.contains(value));
    }

    /**
     * Replaces every occurrence as a single atomic operation, permitting keys to
     * be exchanged between different positions without intermediate collisions.
     *
     * @param operator non-null transformation applied once per old value
     * @throws NullPointerException if operator is null or produces a null value
     * @throws IllegalArgumentException if the result violates a unique key
     * @throws RuntimeException if a transformer or key extractor throws
     */
    @Override public void replaceAll(UnaryOperator<V> operator) {
        Objects.requireNonNull(operator, "operator");
        List<Entry<V>> candidate = new ArrayList<>();
        IdentityHashMap<Entry<V>, Entry<V>> replacements = new IdentityHashMap<>();
        for (Entry<V> old : entries) {
            Entry<V> updated = new Entry<>(operator.apply(old.value));
            candidate.add(updated);
            replacements.put(old, updated);
        }
        if (!candidate.isEmpty()) publish(candidate, replacements, false);
    }

    /**
     * Sorts all occurrences in place, preserving their identity and membership in
     * tracked sublists while changing their order in the parent list.
     *
     * @param comparator ordering comparator, or null for natural ordering
     * @throws ClassCastException if the selected comparison cannot order the values
     * @throws RuntimeException if comparator throws; the list remains unchanged
     */
    @Override public void sort(Comparator<? super V> comparator) {
        List<Entry<V>> candidate = new ArrayList<>(entries);
        candidate.sort((left, right) -> compareValues(left.value, right.value, comparator));
        if (!sameOrder(candidate, entries)) publish(candidate, Map.of(), true);
    }

    @SuppressWarnings("unchecked")
    private static <V> int compareValues(V left, V right, Comparator<? super V> comparator) {
        return comparator != null ? comparator.compare(left, right)
            : ((Comparable<? super V>) left).compareTo(right);
    }

    private static <V> boolean sameOrder(List<Entry<V>> a, List<Entry<V>> b) {
        for (int i = 0; i < a.size(); i++) if (a.get(i) != b.get(i)) return false;
        return true;
    }

    private Map<Index<V>, Map<Object, List<Entry<V>>>> buildIndexes(List<Entry<V>> candidate) {
        Map<Index<V>, Map<Object, List<Entry<V>>>> result = new IdentityHashMap<>();
        for (Index<V> index : indexes) {
            Map<Object, List<Entry<V>>> groups = new HashMap<>();
            for (Entry<V> entry : candidate) {
                Object key = Objects.requireNonNull(index.definition.extractKey(entry.value),
                    "Null key from " + index.definition.getId());
                List<Entry<V>> group = groups.computeIfAbsent(key, ignored -> new ArrayList<>());
                if (index.definition.isUnique() && !group.isEmpty()) {
                    throw new IllegalArgumentException("Duplicate unique key for "
                        + index.definition.getId() + ": " + key);
                }
                group.add(entry);
            }
            result.put(index, groups);
        }
        return result;
    }

    private void publish(List<Entry<V>> candidate, Map<Entry<V>, Entry<V>> replacements, boolean structural) {
        Map<Index<V>, Map<Object, List<Entry<V>>>> next = buildIndexes(candidate);
        Set<Entry<V>> surviving = Collections.newSetFromMap(new IdentityHashMap<>());
        surviving.addAll(candidate);
        entries.clear();
        entries.addAll(candidate);
        for (Index<V> index : indexes) index.groups = next.get(index);
        mutationVersion++;
        if (structural) modCount++;
        cleanSubLists();
        for (WeakReference<TrackedSubList> weak : subLists) {
            TrackedSubList view = weak.get();
            if (view != null) view.synchronizeMembers(surviving, replacements);
        }
    }

    private void cleanSubLists() {
        Reference<? extends TrackedSubList> ref;
        while ((ref = subListQueue.poll()) != null) subLists.remove(ref);
    }

    private TrackedSubList track(List<Entry<V>> membership, int insertionHint) {
        cleanSubLists();
        TrackedSubList view = new TrackedSubList(membership, insertionHint);
        subLists.add(new WeakReference<>(view, subListQueue));
        return view;
    }

    /**
     * Returns a backed membership view of the specified current positional range.
     * Subsequent parent insertions do not become sublist members; deletions from
     * any view remove the same occurrences from every active sublist. Insertion
     * through the returned view also inserts into the parent and into this view.
     *
     * @param fromIndex inclusive index in the current list
     * @param toIndex exclusive index in the current list
     * @return independently tracked, live membership sublist, weakly registered
     * @throws IndexOutOfBoundsException if a bound is outside the list or
     *                                   fromIndex exceeds toIndex
     */
    @Override public List<V> subList(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, entries.size());
        return track(new ArrayList<>(entries.subList(fromIndex, toIndex)), fromIndex);
    }

    /** {@inheritDoc} */
    @Override public AIiMultiMappedList<V> copyRange(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, entries.size());
        List<AIiKeyDefinition<V, ?>> definitions = new ArrayList<>();
        for (Index<V> index : indexes) definitions.add(index.definition);
        AIcMultiMappedList<V> copied = new AIcMultiMappedList<>(definitions);
        copied.addAll(new ArrayList<>(this.subList(fromIndex, toIndex)));
        return copied;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <K> Map<K, List<V>> mapByKeyType(AIiKeyDefinition<V, K> definition) {
        Objects.requireNonNull(definition, "definition");
        Index<V> index = byDefinition.get(definition);
        if (index == null) throw new IllegalArgumentException("Unregistered key definition: " + definition.getId());
        if (index.view == null) index.view = new MapView<>(index);
        return (Map<K, List<V>>) index.view;
    }

    private List<V> snapshot(Collection<Entry<V>> group) {
        List<V> result = new ArrayList<>(group.size());
        for (Entry<V> entry : group) result.add(entry.value);
        return Collections.unmodifiableList(result);
    }

    private List<V> removeGroup(Index<V> index, Object key) {
        List<Entry<V>> matching = index.groups.get(key);
        if (matching == null) return null;
        List<V> result = snapshot(matching);
        Set<Entry<V>> removed = Collections.newSetFromMap(new IdentityHashMap<>());
        removed.addAll(matching);
        List<Entry<V>> candidate = new ArrayList<>();
        for (Entry<V> entry : entries) if (!removed.contains(entry)) candidate.add(entry);
        publish(candidate, Map.of(), true);
        return result;
    }

    private final class MapView<K> extends AbstractMap<K, List<V>> {
        private final Index<V> index;
        private final Set<Map.Entry<K, List<V>>> entrySet = new MapEntries();

        private MapView(Index<V> index) { this.index = index; }
        /** {@inheritDoc} */
        @Override public int size() { return index.groups.size(); }
        /** {@inheritDoc} */
        @Override public boolean containsKey(Object key) { return index.groups.containsKey(key); }
        /** {@inheritDoc} */
        @Override public List<V> get(Object key) {
            List<AIcMultiMappedList.Entry<V>> group = index.groups.get(key);
            return group == null ? null : snapshot(group);
        }
        /** {@inheritDoc} */
        @Override public List<V> put(K key, List<V> values) {
            throw new UnsupportedOperationException("Insert values through the parent list");
        }
        /** {@inheritDoc} */
        @Override public List<V> remove(Object key) { return removeGroup(index, key); }
        /** {@inheritDoc} */
        @Override public void clear() { AIcMultiMappedList.this.clear(); }
        /** {@inheritDoc} */
        @Override public Set<Map.Entry<K, List<V>>> entrySet() { return entrySet; }

        private final class MapEntries extends AbstractSet<Map.Entry<K, List<V>>> {
            /** {@inheritDoc} */
            @Override public int size() { return index.groups.size(); }
            /** {@inheritDoc} */
            @Override public void clear() { MapView.this.clear(); }
            /** {@inheritDoc} */
            @Override public Iterator<Map.Entry<K, List<V>>> iterator() {
                List<Object> keys = new ArrayList<>(index.groups.keySet());
                return new Iterator<>() {
                    private int cursor;
                    private Object last;
                    private boolean removable;
                    private long expectedVersion = mutationVersion;

                    private void check() {
                        if (expectedVersion != mutationVersion) {
                            throw new java.util.ConcurrentModificationException();
                        }
                    }
                    /** {@inheritDoc} */
                    @Override public boolean hasNext() { check(); return cursor < keys.size(); }
                    /** {@inheritDoc} */
                    @SuppressWarnings("unchecked")
                    @Override public Map.Entry<K, List<V>> next() {
                        check();
                        if (!hasNext()) throw new java.util.NoSuchElementException();
                        Object key = keys.get(cursor++);
                        last = key;
                        removable = true;
                        return new SimpleImmutableEntry<>((K) key, MapView.this.get(key));
                    }
                    /** {@inheritDoc} */
                    @Override public void remove() {
                        check();
                        if (!removable) throw new IllegalStateException("next() not called");
                        MapView.this.remove(last);
                        expectedVersion = mutationVersion;
                        removable = false;
                    }
                };
            }
        }
    }

    private final class TrackedSubList extends AbstractList<V> {
        private final List<Entry<V>> members;
        private int insertionHint;

        private TrackedSubList(List<Entry<V>> members, int insertionHint) {
            this.members = members;
            this.insertionHint = insertionHint;
        }

        private void synchronizeMembers(Set<Entry<V>> surviving, Map<Entry<V>, Entry<V>> replacements) {
            boolean changed = false;
            for (int i = members.size() - 1; i >= 0; i--) {
                Entry<V> old = members.get(i);
                Entry<V> replacement = replacements.get(old);
                if (replacement != null) {
                    members.set(i, replacement);
                } else if (!surviving.contains(old)) {
                    members.remove(i);
                    changed = true;
                }
            }
            if (changed) modCount++;
        }

        /** {@inheritDoc} */
        @Override public int size() { return members.size(); }
        /** {@inheritDoc} */
        @Override public V get(int index) { return members.get(index).value; }
        /** {@inheritDoc} */
        @Override public V set(int index, V value) {
            Entry<V> old = members.get(index);
            replaceEntry(old, value);
            return old.value;
        }
        /** {@inheritDoc} */
        @Override public void add(int index, V value) {
            addAll(index, List.of(Objects.requireNonNull(value, "value")));
        }
        /** {@inheritDoc} */
        @Override public boolean addAll(int index, Collection<? extends V> values) {
            Objects.requireNonNull(values, "values");
            if (index < 0 || index > members.size()) throw new IndexOutOfBoundsException(index);
            List<V> copied = new ArrayList<>(values);
            if (copied.isEmpty()) return false;
            int sourcePosition = insertionPosition(index);
            List<Entry<V>> fresh = insertAll(sourcePosition, copied);
            members.addAll(index, fresh);
            modCount++;
            return true;
        }
        private int insertionPosition(int index) {
            if (index < members.size()) return position(members.get(index));
            if (!members.isEmpty()) return position(members.get(members.size() - 1)) + 1;
            return Math.min(insertionHint, entries.size());
        }
        /** {@inheritDoc} */
        @Override public V remove(int index) {
            Entry<V> old = members.get(index);
            removeEntry(old);
            return old.value;
        }
        /** {@inheritDoc} */
        @Override protected void removeRange(int fromIndex, int toIndex) {
            Objects.checkFromToIndex(fromIndex, toIndex, members.size());
            if (fromIndex == toIndex) return;
            Set<Entry<V>> removed = Collections.newSetFromMap(new IdentityHashMap<>());
            removed.addAll(members.subList(fromIndex, toIndex));
            List<Entry<V>> candidate = new ArrayList<>();
            for (Entry<V> entry : entries) if (!removed.contains(entry)) candidate.add(entry);
            publish(candidate, Map.of(), true);
        }
        /** {@inheritDoc} */
        @Override public List<V> subList(int fromIndex, int toIndex) {
            Objects.checkFromToIndex(fromIndex, toIndex, members.size());
            return track(new ArrayList<>(members.subList(fromIndex, toIndex)), insertionPosition(fromIndex));
        }
        /** {@inheritDoc} */
        @Override public boolean removeIf(Predicate<? super V> predicate) {
            Objects.requireNonNull(predicate, "predicate");
            Set<Entry<V>> removed = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Entry<V> entry : members) if (predicate.test(entry.value)) removed.add(entry);
            if (removed.isEmpty()) return false;
            List<Entry<V>> candidate = new ArrayList<>();
            for (Entry<V> entry : entries) if (!removed.contains(entry)) candidate.add(entry);
            publish(candidate, Map.of(), true);
            return true;
        }
        /** {@inheritDoc} */
        @Override public boolean removeAll(Collection<?> values) {
            Objects.requireNonNull(values, "values");
            return removeIf(values::contains);
        }
        /** {@inheritDoc} */
        @Override public boolean retainAll(Collection<?> values) {
            Objects.requireNonNull(values, "values");
            return removeIf(value -> !values.contains(value));
        }
        /** {@inheritDoc} */
        @Override public void replaceAll(UnaryOperator<V> operator) {
            Objects.requireNonNull(operator, "operator");
            IdentityHashMap<Entry<V>, Entry<V>> changes = new IdentityHashMap<>();
            for (Entry<V> old : members) changes.put(old, new Entry<>(operator.apply(old.value)));
            List<Entry<V>> candidate = new ArrayList<>(entries);
            for (int i = 0; i < candidate.size(); i++) {
                Entry<V> replacement = changes.get(candidate.get(i));
                if (replacement != null) candidate.set(i, replacement);
            }
            if (!changes.isEmpty()) publish(candidate, changes, false);
        }
        /** {@inheritDoc} */
        @Override public void sort(Comparator<? super V> comparator) {
            List<Entry<V>> sorted = new ArrayList<>(members);
            sorted.sort((a, b) -> compareValues(a.value, b.value, comparator));
            if (sameOrder(sorted, members)) return;
            List<Entry<V>> candidate = new ArrayList<>(entries);
            Set<Entry<V>> selected = Collections.newSetFromMap(new IdentityHashMap<>());
            selected.addAll(members);
            int next = 0;
            for (int i = 0; i < candidate.size(); i++) {
                if (selected.contains(candidate.get(i))) candidate.set(i, sorted.get(next++));
            }
            publish(candidate, Map.of(), true);
            members.clear();
            members.addAll(sorted);
            modCount++;
        }
    }
}
