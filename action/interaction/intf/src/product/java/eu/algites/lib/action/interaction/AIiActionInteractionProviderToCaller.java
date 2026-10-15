package eu.algites.lib.action.interaction;

/**
 * Direction exclusively exposed to the executing provider.
 * Implementations may also implement the opposite direction, but the public interfaces
 * must remain independent to prevent caller/provider authority leakage.
 * Generic parameters preserve all specialized AAC or application payload types.
 * @param <E> event type
 * @param <F> supported features
 * @param <S> incremental-state result
 * @param <PM> provider-side published message
 * @param <CM> caller-side requested-state snapshot
 */
public interface AIiActionInteractionProviderToCaller<E extends AIiActionInteractionEvent,
        F extends AIiActionInteractionFeatures,
        S,
        PM extends AIiActionInteractionProviderMessage<E, F, S>,
        CM extends AIiActionInteractionCallerMessage> {

    /** Publishes one logical ordered batch of events without altering transaction state. */
    void reportEvents(java.util.List<? extends E> aEvents);

    /** Declares supported features; unsupported caller requests must be rejected. */
    void declareFeatures(F aFeatures);

    /** Marks a logical interim-state change and publishes its typed payload. */
    long publishStateResult(S aStateResult);

    /** Obtains a frozen view of caller preferences and cancellation request. */
    CM callerSnapshot();
}
