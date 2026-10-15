package eu.algites.lib.action.interaction;

/**
 * Immutable snapshot of caller requests directed to an action provider.
 * A cancellation request is not an authoritative cancellation or rollback decision.
 */
public interface AIiActionInteractionCallerMessage {

    /** Monotonic caller-message revision for this interaction. */
    long getRevision();

    /** True after the caller requests cooperative cancellation. */
    boolean isCancellationRequested();

    /** Presentation mode such as FOREGROUND or BACKGROUND. */
    String getPresentationMode();

    /** Requested reporting detail, such as SUMMARY or DETAILED. */
    String getDetailLevel();

    /** Optional minimum reporting interval in milliseconds. */
    Long getReportingIntervalMillis();

    /** Requested state-result delivery mode, if negotiated. */
    String getStateResultDeliveryMode();
}
