package eu.algites.lib.action.execution.engine;

/**
 * Immutable scheduler/retry policy assigned when scheduling an action.
 * There is intentionally no enabled flag: disabled work must not be scheduled.
 * Neither endpoint capacity nor resource maximum is configured here.
 */
public interface AIiActionExecutionPolicy {

    /** Local execution-order barrier among siblings; lower groups precede higher. */
    int getExecutionOrder();

    /** Number of additional attempts after the first attempt. */
    int getRetryCount();

    /** Minimum backoff between attempts, in milliseconds. */
    long getRetryWaitMillis();

    /** Per-attempt timeout in milliseconds, or null for no explicit limit. */
    Long getAttemptTimeoutMillis();

    /** Reaction to a failure not recovered through retries. */
    AIiActionFailurePolicy getFailurePolicy();
}
