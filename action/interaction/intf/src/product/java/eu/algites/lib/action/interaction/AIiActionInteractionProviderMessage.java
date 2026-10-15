package eu.algites.lib.action.interaction;

/**
 * Immutable provider-to-caller snapshot with correlated event and state-result revisions.
 * The state result is independent of the final ActionOutput and may be null or a delta.
 * @param <E> specialized event contract
 * @param <F> specialized feature contract
 * @param <S> specialized interim-state result
 */
public interface AIiActionInteractionProviderMessage<E extends AIiActionInteractionEvent, F extends AIiActionInteractionFeatures, S> {

    /** Monotonic interaction revision. */
    long getInteractionRevision();

    /** State-result revision, which changes only on logical state-result updates. */
    long getStateResultRevision();

    /** Immutable batch of events belonging to this notification. */
    java.util.List<? extends E> getEvents();

    /** Provider capabilities as last declared. */
    F getFeatures();

    /** Optional full or delta interim state-result payload. */
    S getStateResult();

    /** Delivery mode used for this state-result payload. */
    String getStateResultDeliveryMode();
}
