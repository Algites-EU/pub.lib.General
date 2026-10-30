package eu.algites.lib.common.list;

import java.util.List;
import java.util.Map;

/**
 * Ordered mutable list with multiple key-based, unordered live map views over the
 * same element occurrences. Each map maps a key to an immutable snapshot list of
 * matching values. Map views are cached by key-definition object identity; a
 * repeated lookup of the same registered definition returns the same map object.
 *
 * <p>Only the list can insert or replace elements. Map {@code remove(key)} removes
 * every matching occurrence from the underlying list and every other map, while
 * map {@code put}, {@code replace}, and insertion by map entries are unsupported.
 * An ordinary list removal also updates all maps. A unique key definition rejects
 * duplicate keys; grouping definitions permit duplicate keys. Map value snapshots
 * retain insertion/list order, but map iteration order is not specified.</p>
 *
 * <p>{@code subList} returns a tracked membership view rather than a positional
 * range: additions to the parent do not add members to an existing sublist;
 * removals from any supported path remove the corresponding occurrences from
 * all active sublists. Changes made directly through a sublist propagate to the
 * parent. Such sublists are tracked weakly; they do not keep abandoned sublists
 * alive. This behavior deliberately differs from an ordinary range-based
 * {@link List#subList(int, int)} after external structural modifications.</p>
 *
 * <p>Elements must be non-null. Key attributes, extracted key objects, and their
 * equals/hashCode behavior must not change while stored; modifying them outside
 * this list is a caller contract violation and can invalidate lookups. This
 * implementation does not provide concurrent mutation safety.</p>
 *
 * @param <V> non-null stored value type
 * @author linhart1
 */
public interface AIiMultiMappedList<V> extends List<V> {
    /**
     * Returns the cached, live map view for a registered key definition. The map
     * has read-only snapshot lists as its values. The returned map allows removal
     * of an entire key group, but does not support insertion or replacement.
     *
     * @param definition exact key definition object registered with this list
     * @param <K> type of key exposed by this definition
     * @return cached live map view, with unordered keys and snapshot value lists
     * @throws NullPointerException if definition is null
     * @throws IllegalArgumentException if definition was not registered
     */
    <K> Map<K, List<V>> mapByKeyType(AIiKeyDefinition<V, K> definition);

    /**
     * Resolve a registered key definition by its schema identity. This returns
     * the exact definition instance required by {@link #mapByKeyType(AIiKeyDefinition)},
     * not a copy whose identity would fail map lookup.
     *
     * @param id nonblank index identifier
     * @return registered definition, or null when no index has this identifier
     * @throws NullPointerException if id is null
     */
    AIiKeyDefinition<V, ?> keyDefinition(String id);

    /**
     * Creates a detached, shallow copy of a positional range, preserving the same
     * key definitions but not sharing membership or map indexes with this list.
     * Stored value objects themselves are shared, not cloned.
     *
     * @param fromIndex inclusive position in this list
     * @param toIndex exclusive position in this list
     * @return independently mutable multi-mapped list containing the range
     * @throws IndexOutOfBoundsException if either bound is outside the list or
     *                                   fromIndex is greater than toIndex
     */
    AIiMultiMappedList<V> copyRange(int fromIndex, int toIndex);
}
