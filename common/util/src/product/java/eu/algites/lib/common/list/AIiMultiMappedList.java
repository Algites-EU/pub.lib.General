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
public interface AIiMultiMappedList<V> extends AIiBoundedList<V> {
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
     * Returns the number of registered key definitions (not the number of key values).
     *
     * @return zero for an unindexed list, otherwise the number of definitions
     */
    int getKeyCount();

    /**
     * Indicates whether exactly one key definition is registered.
     *
     * @return true precisely when {@link #getKeyCount()} is one
     */
    default boolean isOneKeyList() {
        return getKeyCount() == 1;
    }

    /**
     * Reports whether key definitions may be registered and removed at runtime.
     *
     * @return true for a dynamic-definition list, false for a fixed-definition list
     */
    default boolean isDynamicKeyDefsList() {
        return false;
    }

    /**
     * Returns the cached map of the list's only registered key definition.
     * Its values are snapshot {@code List<V>} groups even for unique keys.
     *
     * @param <K> the key type, which the caller must know from the definition
     * @return live map of keys to immutable snapshot lists
     * @throws IllegalStateException if there are zero or more than one key definitions
     * @throws ClassCastException on use if the caller assumes a different key type
     */
    <K> Map<K, List<V>> getOneKeyMap();

    /**
     * Registers a new key definition and atomically indexes existing elements.
     *
     * @param definition non-null key definition with unique, nonblank id and identity
     * @throws IllegalStateException if definitions are fixed
     * @throws IllegalArgumentException if its id/identity is duplicated or any
     *         existing value violates the new definition
     * @throws NullPointerException if definition is null
     */
    void addKeyDefinition(AIiKeyDefinition<V, ?> definition);

    /**
     * Removes the definition identified by its stable id without removing values.
     * Any previously retrieved map view for that definition becomes invalid.
     *
     * @param id non-null definition id
     * @return true if a matching definition was present, false otherwise
     * @throws IllegalStateException if definitions are fixed
     * @throws NullPointerException if id is null
     */
    boolean removeKeyDefinition(String id);

    /**
     * Unregisters all key definitions while retaining list values.
     * Previously returned map views become invalid.
     *
     * @throws IllegalStateException if definitions are fixed
     */
    void clearKeyDefinitions();

    /**
     * Looks up a registered definition id and returns its cached live map.
     *
     * @param id registered definition id
     * @param <K> expected key type of that definition
     * @return live map of key to snapshot group
     * @throws IllegalArgumentException if no definition has this id
     * @throws NullPointerException if id is null
     */
    @SuppressWarnings("unchecked")
    default <K> Map<K, List<V>> mapByKeyId(String id) {
        var definition = keyDefinition(id);
        if (definition == null) throw new IllegalArgumentException("Unknown key definition: " + id);
        return mapByKeyType((AIiKeyDefinition<V, K>) definition);
    }

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
