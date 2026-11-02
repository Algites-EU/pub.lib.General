package eu.algites.lib.common.list;

/**
 * Defines one stable, named lookup projection over values held by a multi-mapped list.
 * A definition identifies a single map view by object identity. Callers must use the
 * exact registered definition object, not a separately constructed definition with
 * the same identifier. Required extracted keys must be non-null; optional definitions may return null,
 * which groups entries with absent keys without enforcing uniqueness. Non-null keys
 * retain stable equality and hash
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
     * @return extracted key, or null only when {@link #isRequired()} is false
     * @throws RuntimeException if extraction is impossible for the supplied value
     */
    K extractKey(V value);

    /**
     * Determines whether at most one value may share each extracted key.
     *
     * @return true for unique keys; false for grouping keys
     */
    boolean isUnique();

    /**
     * Whether the key must exist for every value. Unique optional keys allow
     * multiple absent keys in the null group while checking non-null keys.
     *
     * @return true if a missing key makes insertion invalid
     */
    default boolean isRequired() { return true; }
}
