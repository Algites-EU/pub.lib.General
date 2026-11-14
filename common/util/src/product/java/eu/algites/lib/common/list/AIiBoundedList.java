package eu.algites.lib.common.list;

import eu.algites.lib.common.validation.AIiValidatable;
import java.util.List;
import java.util.function.Consumer;

/**
 * A mutable list with inclusive nullable cardinality bounds and independently
 * reported validity. Mutation policy prevents worsening of the distance from
 * either bound while allowing incomplete lists to be constructed incrementally.
 * An empty list whose minimum is positive is therefore constructible, although
 * not yet valid. Clients can explicitly check validity when the graph is complete.
 *
 * @param <T> value type
 * @author linhart1
 */
public interface AIiBoundedList<T> extends List<T>, AIiValidatable<AIcdBoundedListValidationIssue> {
    /** @return inclusive minimum size, or null when unconstrained */
    Integer getMinSize();
    /** @return inclusive maximum size, or null when unconstrained */
    Integer getMaxSize();
    /** @return policy governing changes relative to current constraint violations */
    AInBoundsMutationPolicy getMutationPolicy();
    /**
     * Edit a detached candidate and publish it only if the callback returns
     * successfully and its final size does not worsen the current violation.
     * Intermediate candidate states need not satisfy the bounds.
     *
     * @param aChange edit callback; it must not mutate the originating list
     * @throws NullPointerException if callback is null
     * @throws IllegalStateException when the resulting size worsens a bound
     */
    void modifyAtomically(Consumer<List<T>> aChange);
}
