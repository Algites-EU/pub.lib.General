package eu.algites.lib.action.execution.resource;

/**
 * Named, engine-wide capacity limitation shared by all plans in that engine.
 * The source endpoint owns configuration; actions only expose resource needs
 * resolved by the endpoint adapter. The limit is NOT an action policy.
 */
public interface AIiExecutionResource {

    /** Stable resource identity across plans using the same physical endpoint. */
    String getResourceId();

    /** Number of concurrent permitted usages; strictly positive. */
    int getMaxConcurrentUsages();
}
