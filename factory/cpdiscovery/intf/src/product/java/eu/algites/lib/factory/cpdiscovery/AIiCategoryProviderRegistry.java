package eu.algites.lib.factory.cpdiscovery;

/** Atomic catalog of factory implementations discovered from explicitly registered scopes. */
public interface AIiCategoryProviderRegistry {
    <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                         String aCategory, C aContext);

    <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                         String aCategory, String aProviderId, C aContext);

    void registerClassLoaderFactories(ClassLoader aClassLoader);
    void unregisterClassLoaderFactories(ClassLoader aClassLoader);
    void registerModuleLayerFactories(ModuleLayer aLayer);
    void unregisterModuleLayerFactories(ModuleLayer aLayer);
}
