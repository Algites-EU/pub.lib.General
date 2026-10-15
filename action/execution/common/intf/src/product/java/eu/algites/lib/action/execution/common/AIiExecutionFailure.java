package eu.algites.lib.action.execution.common;

/**
 * Immutable structured description of a failure or an uncertain external effect.
 * Does not retain Throwable as its persistence format; the original exception can
 * be kept separately in process-local diagnostics.
 */
public interface AIiExecutionFailure {

    /** Domain-specific error identifier. */
    String getCode();

    /** Useful diagnostic message. */
    String getMessage();

    /** Qualified runtime exception class name if applicable. */
    String getExceptionClassName();

    /** True when the external effect cannot be determined conclusively. */
    boolean isEffectUnknown();
}
