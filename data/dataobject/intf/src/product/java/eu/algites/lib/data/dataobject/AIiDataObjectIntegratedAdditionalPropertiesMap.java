package eu.algites.lib.data.dataobject;

import java.util.Map;

/**
 * A live, writable view of the additional (dynamically named) properties of one
 * {@link AIiDataObject}.
 *
 * <p>Only properties not declared by the containing data object's contract are
 * visible through this map. In particular, {@link #get(Object)} returns
 * {@code null} and {@link #containsKey(Object)} returns {@code false} for a
 * declared field name, regardless of whether that field is currently set.
 * {@link #remove(Object)} on such a name has no effect. Conversely, inserting
 * an additional property whose name is reserved by a declared field is rejected
 * with {@link IllegalArgumentException}, even when that field is currently unset.
 * The restriction is based on serialized property names (such as the
 * {@code fieldName} of {@link AIaDataObjectField}), including inherited fields,
 * not on Java getter names or on the fields' current presence.
 *
 * <p>The map is backed by the containing object's mutable state, not a snapshot.
 * Changes made through it become immediately visible in subsequent map reads.
 * Its entries do not include declared properties and do not shadow them. A
 * conforming implementation must enforce the reserved-name invariant on every
 * mutation path, including bulk operations and map-entry updates. Serialization
 * into the same object-property namespace is the responsibility of the data
 * object's format-aware runtime.
 *
 * <p>As with {@link Map}, a returned {@code null} does not distinguish an absent
 * key from a present key mapped to {@code null}; use {@link #containsKey(Object)}
 * to distinguish these cases. The map does not guarantee thread safety.
 *
 * @param <V> allowed value type of dynamically named properties
 * @author linhart1
 */
public interface AIiDataObjectIntegratedAdditionalPropertiesMap<V> extends Map<String, V> {
}
