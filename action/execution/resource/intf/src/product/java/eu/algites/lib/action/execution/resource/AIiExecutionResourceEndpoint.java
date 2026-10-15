package eu.algites.lib.action.execution.resource;

/**
 * Capability implemented by an external endpoint or its scheduling adapter.
 * Publication and subscription limits are configured separately on their
 * respective endpoint definitions; they are mapped into shared resource capacity.
 */
public interface AIiExecutionResourceEndpoint {

    /** Stable endpoint resource declarations, not per-action configuration values. */
    java.util.List<? extends AIiExecutionResource> getExecutionResources();

    /** Requirements for an operation type; values are fixed before plan execution. */
    java.util.List<? extends AIiExecutionResourceRequirement> getRequirementsForOperation(
            String aOperationKind);
}
