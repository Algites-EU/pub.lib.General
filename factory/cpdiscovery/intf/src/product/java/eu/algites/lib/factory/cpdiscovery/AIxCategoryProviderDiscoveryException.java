package eu.algites.lib.factory.cpdiscovery;

/** Unsuccessful category/provider factory discovery or creation. */
public class AIxCategoryProviderDiscoveryException extends RuntimeException {
    public AIxCategoryProviderDiscoveryException(String aMessage) { super(aMessage); }
    public AIxCategoryProviderDiscoveryException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
