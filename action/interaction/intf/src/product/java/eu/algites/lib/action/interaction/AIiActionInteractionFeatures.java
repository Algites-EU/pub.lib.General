package eu.algites.lib.action.interaction;

/**
 * Immutable declaration of provider-supported interaction capabilities.
 * This advertises what the provider supports, not what the caller currently requests.
 */
public interface AIiActionInteractionFeatures {

    /** Whether progress reporting is supported. */
    boolean supportsProgressReporting();

    /** Whether provider-side cooperative cancellation can be requested. */
    boolean supportsCancellation();

    /** Whether detail-level changes may be requested. */
    boolean supportsDetailLevel();

    /** Whether reporting frequency may be configured. */
    boolean supportsReportingInterval();

    /** Symbolic names of supported state-result delivery modes. */
    java.util.Set<String> getSupportedStateResultDeliveryModes();
}
