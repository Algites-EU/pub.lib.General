package eu.algites.lib.factory.cpdiscovery;

/** No unambiguous implementation is available for the requested category. */
public class AIxCategoryProviderUnavailableException extends AIxCategoryProviderDiscoveryException {
    public AIxCategoryProviderUnavailableException(String aMessage) { super(aMessage); }
    public AIxCategoryProviderUnavailableException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
