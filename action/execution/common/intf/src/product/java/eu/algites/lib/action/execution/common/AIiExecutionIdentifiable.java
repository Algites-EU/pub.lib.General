package eu.algites.lib.action.execution.common;

/**
 * Stable identity within an execution plan or across persisted recovery where supported.
 * An identity is not a Java object identity and must remain unchanged through all retries.
 */
public interface AIiExecutionIdentifiable {

    /** Stable execution identity. */
    String getExecutionId();
}
