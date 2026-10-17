package eu.algites.lib.factory.cpdiscovery;

/** Conflicting factory or provider default declarations. */
public class AIxCategoryProviderConflictException extends AIxCategoryProviderDiscoveryException {
    public AIxCategoryProviderConflictException(String aMessage) { super(aMessage); }
    public AIxCategoryProviderConflictException(String aMessage, Throwable aCause) { super(aMessage, aCause); }
}
