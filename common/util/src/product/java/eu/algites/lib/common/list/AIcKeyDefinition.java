package eu.algites.lib.common.list;

import java.util.Objects;
import java.util.function.Function;

/**
 * Immutable key definition backed by an application-supplied extraction function.
 * Composite keys can be immutable Java records with ordinary record equality and
 * hashCode; the collection does not require a special composite-key interface.
 * Definition objects have identity semantics and are not interchangeable merely
 * because their identifiers are equal.
 *
 * @param <V> type of the source value
 * @param <K> hashable type of the extracted key
 * @author linhart1
 */
public final class AIcKeyDefinition<V, K> implements AIiKeyDefinition<V, K> {
    private final String id;
    private final Function<? super V, ? extends K> extractor;
    private final boolean unique;
    private final boolean required;

    /**
     * Creates a key definition whose extractor is evaluated when values are added
     * or replaced, not during ordinary map reads.
     *
     * @param id non-null, non-blank diagnostic identifier, unique within its list
     * @param extractor non-null function producing a non-null, stable key
     * @param unique whether duplicate keys are forbidden
     * @throws NullPointerException if the identifier or extractor is null
     * @throws IllegalArgumentException if the identifier is blank
     */
    public AIcKeyDefinition(String id, Function<? super V, ? extends K> extractor, boolean unique) {
        this(id, extractor, unique, true);
    }

    /**
     * Defines a required or optional key without changing its uniqueness semantics.
     *
     * @param id nonblank index name
     * @param extractor pure extraction function, permitted to return null only if optional
     * @param unique whether non-null duplicate keys are forbidden
     * @param required whether every value must supply a non-null key
     * @throws NullPointerException if id or extractor is null
     * @throws IllegalArgumentException if id is blank
     */
    public AIcKeyDefinition(String id, Function<? super V, ? extends K> extractor,
            boolean unique, boolean required) {
        this.id = Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Key definition id must not be blank");
        }
        this.extractor = Objects.requireNonNull(extractor, "extractor");
        this.unique = unique;
        this.required = required;
    }

    /** {@inheritDoc} */
    @Override public String getId() { return id; }

    /** {@inheritDoc} */
    @Override public K extractKey(V value) { return extractor.apply(value); }

    /** {@inheritDoc} */
    @Override public boolean isUnique() { return unique; }

    /** {@inheritDoc} */
    @Override public boolean isRequired() { return required; }
}
