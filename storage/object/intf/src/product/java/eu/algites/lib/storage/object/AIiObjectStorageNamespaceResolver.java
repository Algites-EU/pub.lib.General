package eu.algites.lib.storage.object;

/**
 * Maps an application's logical storage designation to a permission-scoped
 * transport provider. Configuration and credentials remain outside this API.
 * This optional integration boundary can be backed by a provider registry.
 */
public interface AIiObjectStorageNamespaceResolver<S extends AIiObjectStorage> {

    /**
     * Resolves a preconfigured nonsecret logical namespace name. Authorization
     * must be handled by the integration and never embedded in the name.
     */
    S resolve(String aNamespaceId);
}
