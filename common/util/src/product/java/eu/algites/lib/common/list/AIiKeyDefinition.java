package eu.algites.lib.common.list;

/**
 * Defines one stable, named lookup projection over values held by a multi-mapped list.
 * A definition identifies a single map view by object identity. Callers must use the
 * exact registered definition object, not a separately constructed definition with
 * the same identifier. Extracted keys must be non-null and their equality and hash
 * codes must remain unchanged while the corresponding value is in the collection.
 *
 * <p>Definitions may be unique (as with an XML {@code xs:key} or a JSON object
 * property name) or non-unique (as with grouping by a shared category). For a
 * unique definition, duplicate extracted keys are rejected before any mutation
 * becomes visible. For a non-unique definition, a key can select many values.</p>
 *
 * @param <V> type of the stored value
 * @param <K> type of this definition's extracted, hashable key
 * @author linhart1
 */
public interface AIiKeyDefinition<V, K> {
    /**
     * Returns the stable diagnostic identifier for this key definition.
     *
     * @return non-empty unique identifier within a multi-mapped list
     */
    String getId();

    /**
     * Extracts a lookup key from a value without modifying the value. Implementations
     * must be deterministic for values whose key attributes have not changed.
     *
     * @param value non-null value to inspect
     * @return non-null key with stable equals/hashCode behavior while inserted
     * @throws RuntimeException if extraction is impossible for the supplied value
     */
    K extractKey(V value);

    /**
     * Determines whether at most one value may share each extracted key.
     *
     * @return true for unique keys; false for grouping keys
     */
    boolean isUnique();
}
