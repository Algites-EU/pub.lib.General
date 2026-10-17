package eu.algites.lib.factory.cpdiscovery;

/** Unsuccessful category/provider factory discovery or creation. */
public class AIxCategoryProviderDiscoveryException extends RuntimeException {
    /**
     * Creates a category provider discovery exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     */
    public AIxCategoryProviderDiscoveryException(String aMessage) { super(aMessage); }
    /**
     * Creates a category provider discovery exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     * @param aCause underlying cause of the failure
     */
    public AIxCategoryProviderDiscoveryException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
