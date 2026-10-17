package eu.algites.lib.factory.cpdiscovery;

/** No unambiguous implementation is available for the requested category. */
public class AIxCategoryProviderUnavailableException extends AIxCategoryProviderDiscoveryException {
    /**
     * Creates a category provider unavailable exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     */
    public AIxCategoryProviderUnavailableException(String aMessage) { super(aMessage); }
    /**
     * Creates a category provider unavailable exception instance with the supplied configuration.
     * @param aMessage message text describing the failure
     * @param aCause underlying cause of the failure
     */
    public AIxCategoryProviderUnavailableException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
