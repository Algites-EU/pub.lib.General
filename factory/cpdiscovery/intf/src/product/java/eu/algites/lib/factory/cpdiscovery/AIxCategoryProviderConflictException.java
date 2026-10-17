package eu.algites.lib.factory.cpdiscovery;

/** Conflicting factory or provider default declarations. */
public class AIxCategoryProviderConflictException extends AIxCategoryProviderDiscoveryException {
    /**
     * Creates a category provider conflict exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     */
    public AIxCategoryProviderConflictException(String aMessage) { super(aMessage); }
    /**
     * Creates a category provider conflict exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     * @param aCause underlying cause of the failure
     */
    public AIxCategoryProviderConflictException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
