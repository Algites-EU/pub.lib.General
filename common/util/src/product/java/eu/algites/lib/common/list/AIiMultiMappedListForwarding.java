package eu.algites.lib.common.list;

import java.util.List;
import java.util.Map;

/**
 * Delegating implementation bridge for schema-generated SmartDataObject types
 * that also model an ordered multi-indexed list.
 *
 * <p>Java permits one concrete superclass. Generated schema objects already
 * extend the format-specific SmartDataObject runtime, so they cannot also
 * extend {@link AIcMultiMappedList}. This mixin supplies actual list and index
 * behavior through the same authoritative delegate, without weakening the
 * published {@link AIiMultiMappedList} contract or losing XML/JSON/YAML
 * SmartDataObject inheritance.</p>
 *
 * <p>The implementor must expose the identical delegate instance for its
 * entire lifetime. It must also preserve the {@link List} equals/hashCode
 * contract in its concrete class; interface defaults cannot override Object
 * methods. A schema-created instance with no registered definitions exposes
 * an ordinary list until definitions are supplied at construction.</p>
 *
 * @param <V> non-null stored element type
 * @author linhart1
 */
public interface AIiMultiMappedListForwarding<V> extends AIiMultiMappedList<V> {
    /**
     * Return the sole authoritative list/index store.
     *
     * @return the stable, non-null delegate for all operations
     */
    AIiMultiMappedList<V> multiMappedDelegate();

    /** {@inheritDoc} */
    @Override default <K> Map<K, List<V>> mapByKeyType(AIiKeyDefinition<V, K> aDefinition) {
        return multiMappedDelegate().mapByKeyType(aDefinition);
    }

    /** {@inheritDoc} */
    @Override default AIiKeyDefinition<V, ?> keyDefinition(String aIdentifier) {
        return multiMappedDelegate().keyDefinition(aIdentifier);
    }

    /** {@inheritDoc} */
    @Override default AIiMultiMappedList<V> copyRange(int aFrom, int aTo) {
        return multiMappedDelegate().copyRange(aFrom, aTo);
    }

    /** {@inheritDoc} */
    @Override default int size() {
        return multiMappedDelegate().size();
    }

    /** {@inheritDoc} */
    @Override default boolean isEmpty() {
        return multiMappedDelegate().isEmpty();
    }

    /** {@inheritDoc} */
    @Override default boolean contains(Object aValue) {
        return multiMappedDelegate().contains(aValue);
    }

    /** {@inheritDoc} */
    @Override default java.util.Iterator<V> iterator() {
        return multiMappedDelegate().iterator();
    }

    /** {@inheritDoc} */
    @Override default Object[] toArray() {
        return multiMappedDelegate().toArray();
    }

    /** {@inheritDoc} */
    @Override default <T> T[] toArray(T[] aTarget) {
        return multiMappedDelegate().toArray(aTarget);
    }

    /** {@inheritDoc} */
    @Override default boolean add(V aValue) {
        return multiMappedDelegate().add(aValue);
    }

    /** {@inheritDoc} */
    @Override default boolean remove(Object aValue) {
        return multiMappedDelegate().remove(aValue);
    }

    /** {@inheritDoc} */
    @Override default boolean containsAll(java.util.Collection<?> aItems) {
        return multiMappedDelegate().containsAll(aItems);
    }

    /** {@inheritDoc} */
    @Override default boolean addAll(java.util.Collection<? extends V> aItems) {
        return multiMappedDelegate().addAll(aItems);
    }

    /** {@inheritDoc} */
    @Override default boolean addAll(int aIndex, java.util.Collection<? extends V> aItems) {
        return multiMappedDelegate().addAll(aIndex, aItems);
    }

    /** {@inheritDoc} */
    @Override default boolean removeAll(java.util.Collection<?> aItems) {
        return multiMappedDelegate().removeAll(aItems);
    }

    /** {@inheritDoc} */
    @Override default boolean retainAll(java.util.Collection<?> aItems) {
        return multiMappedDelegate().retainAll(aItems);
    }

    /** {@inheritDoc} */
    @Override default void clear() {
        multiMappedDelegate().clear();
    }

    /** {@inheritDoc} */
    @Override default V get(int aIndex) {
        return multiMappedDelegate().get(aIndex);
    }

    /** {@inheritDoc} */
    @Override default V set(int aIndex, V aValue) {
        return multiMappedDelegate().set(aIndex, aValue);
    }

    /** {@inheritDoc} */
    @Override default void add(int aIndex, V aValue) {
        multiMappedDelegate().add(aIndex, aValue);
    }

    /** {@inheritDoc} */
    @Override default V remove(int aIndex) {
        return multiMappedDelegate().remove(aIndex);
    }

    /** {@inheritDoc} */
    @Override default int indexOf(Object aValue) {
        return multiMappedDelegate().indexOf(aValue);
    }

    /** {@inheritDoc} */
    @Override default int lastIndexOf(Object aValue) {
        return multiMappedDelegate().lastIndexOf(aValue);
    }

    /** {@inheritDoc} */
    @Override default java.util.ListIterator<V> listIterator() {
        return multiMappedDelegate().listIterator();
    }

    /** {@inheritDoc} */
    @Override default java.util.ListIterator<V> listIterator(int aIndex) {
        return multiMappedDelegate().listIterator(aIndex);
    }

    /** {@inheritDoc} */
    @Override default java.util.List<V> subList(int aFrom, int aTo) {
        return multiMappedDelegate().subList(aFrom, aTo);
    }

    /** {@inheritDoc} */
    @Override default void replaceAll(java.util.function.UnaryOperator<V> aOperator) {
        multiMappedDelegate().replaceAll(aOperator);
    }

    /** {@inheritDoc} */
    @Override default void sort(java.util.Comparator<? super V> aComparator) {
        multiMappedDelegate().sort(aComparator);
    }

    /** {@inheritDoc} */
    @Override default boolean removeIf(java.util.function.Predicate<? super V> aFilter) {
        return multiMappedDelegate().removeIf(aFilter);
    }

}
