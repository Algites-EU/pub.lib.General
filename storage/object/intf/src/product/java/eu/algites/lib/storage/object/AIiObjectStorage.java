package eu.algites.lib.storage.object;

/**
 * Identity shared by the independently granted object-storage capabilities.
 * A storage instance represents one configured physical namespace and authorized
 * root/prefix; keys passed to methods are relative to this namespace.
 *
 * <p>Implementations must be safe for concurrent independent callers. Implementers
 * must not infer security privileges from interface identity: every operation must
 * still be authorized by the actual configured backend credentials.</p>
 *
 * <p>Identity is stable across actions and retries using the same configured
 * namespace. It contains no credentials and is not an object URI.</p>
 */
public interface AIiObjectStorage {

    /**
     * Stable nonsecret namespace identity suitable for diagnostics and coordination.
     * Two configurations with different authorization roots must not share an identity.
     */
    String getStorageId();
}
