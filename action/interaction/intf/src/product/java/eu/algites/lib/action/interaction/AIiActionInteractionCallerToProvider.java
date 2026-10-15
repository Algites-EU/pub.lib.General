package eu.algites.lib.action.interaction;

/**
 * Direction exclusively exposed to the caller/controller.
 * This interface MUST NOT extend the provider-to-caller interface; a caller cannot
 * report provider progress or declare provider capabilities.
 * @param <E> event type
 * @param <F> supported features
 * @param <S> interim state-result type
 * @param <PM> provider snapshot type
 * @param <CM> caller preference message type
 */
public interface AIiActionInteractionCallerToProvider<E extends AIiActionInteractionEvent,
        F extends AIiActionInteractionFeatures,
        S,
        PM extends AIiActionInteractionProviderMessage<E, F, S>,
        CM extends AIiActionInteractionCallerMessage> {

    /** Requests cooperative cancellation, not automatic transaction rollback. */
    void requestCancellation();

    /** Requests a presentation mode; not an asynchronous execution policy. */
    void setPresentationMode(String aMode);

    /** Requests a supported level of diagnostic detail. */
    void setDetailLevel(String aLevel);

    /** Requests a supported progress reporting interval in milliseconds. */
    void setReportingIntervalMillis(Long aInterval);

    /** Requests a supported interim-state result delivery mode. */
    void setStateResultDeliveryMode(String aMode);

    /** Requests an up-to-date interim-state result, if supported. */
    void requestStateResult();

    /** Acknowledges receipt of the interim-state result at this revision. */
    void acceptStateResultRevision(long aRevision);

    /** Returns the immutable provider snapshot currently observable by the caller. */
    PM providerSnapshot();

    /** Returns the immutable caller settings snapshot. */
    CM callerSnapshot();
}
