package eu.algites.lib.action.execution.resource;

/**
 * Closeable reservation obtained atomically for the entire requirement set.
 * Reservation release must be idempotent; releasing a lease does not prove
 * that a timed-out remote effect has stopped consuming remote resources.
 */
public interface AIiExecutionResourceLease extends AutoCloseable {

    /** Immutable list of requirements covered by this reservation. */
    java.util.List<? extends AIiExecutionResourceRequirement> getRequirements();

    /** Releases reserved capacity exactly once, even if called repeatedly. */
    @Override
    void close();
}
