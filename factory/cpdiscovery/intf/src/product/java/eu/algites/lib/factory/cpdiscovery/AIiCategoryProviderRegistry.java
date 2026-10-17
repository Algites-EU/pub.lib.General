package eu.algites.lib.factory.cpdiscovery;

/** Atomic catalog of factory implementations discovered from explicitly registered scopes. */
public interface AIiCategoryProviderRegistry {
    /**
     * Returns the instance.
     * @param aFactoryType interface identifying the desired provider factory
     * @param aCategory category of the required provider
     * @param aContext context used to create or select an implementation
     * @return the instance
     */
    <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                         String aCategory, C aContext);

    /**
     * Returns the instance.
     * @param aFactoryType interface identifying the desired provider factory
     * @param aCategory category of the required provider
     * @param aProviderId explicit identifier of the required provider
     * @param aContext context used to create or select an implementation
     * @return the instance
     */
    <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                         String aCategory, String aProviderId, C aContext);

    /**
     * Registers class loader factories in the corresponding registry.
     * @param aClassLoader class loader whose discovered providers are affected
     */
    void registerClassLoaderFactories(ClassLoader aClassLoader);
    /**
     * Removes factories registered through the specified class loader.
     * @param aClassLoader class loader whose discovered providers are affected
     */
    void unregisterClassLoaderFactories(ClassLoader aClassLoader);
    /**
     * Registers module layer factories in the corresponding registry.
     * @param aLayer module layer whose discovered providers are affected
     */
    void registerModuleLayerFactories(ModuleLayer aLayer);
    /**
     * Removes factories registered through the specified module layer.
     * @param aLayer module layer whose discovered providers are affected
     */
    void unregisterModuleLayerFactories(ModuleLayer aLayer);
}
