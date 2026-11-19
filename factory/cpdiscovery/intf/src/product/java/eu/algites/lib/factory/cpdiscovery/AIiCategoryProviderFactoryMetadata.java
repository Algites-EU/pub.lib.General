package eu.algites.lib.factory.cpdiscovery;

/**
 * Read-only metadata describing one discovered specialized factory, its supported
 * category and its provider identity. Inspecting metadata does not create an
 * implementation instance. The concrete metadata representation belongs to the
 * CPDiscovery implementation artifact.
 */
public interface AIiCategoryProviderFactoryMetadata {
    /**
     * Returns the category advertised by this factory registration.
     *
     * @return non-null category identifier
     */
    String category();

    /**
     * Returns the provider registered for the category.
     *
     * @return non-null, stable provider identifier
     */
    String providerId();

    /**
     * Returns the interface implemented by instances produced by this factory.
     *
     * @return non-null implementation interface type
     */
    Class<?> implementationInterfaceType();

    /**
     * Returns the context interface expected by the factory.
     *
     * @return non-null context interface type
     */
    Class<?> contextInterfaceType();

    /**
     * Whether the registered provider is explicitly configured as the category
     * default; this flag does not imply that only one provider is available.
     *
     * @return true when selected as an explicit default provider
     */
    boolean defaultProvider();

    /**
     * Returns the registration source used for diagnostics.
     *
     * @return non-null registration source description
     */
    String source();
}
