package eu.algites.lib.factory.cpdiscovery;

import java.util.List;
import java.util.Set;

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
     * Finds a registered factory without creating an implementation instance. The ordered
     * provider IDs are attempted in priority order, enabling a caller-defined fallback.
     * If the list is null or empty, the configured default is used, or the sole provider
     * if unambiguous. A missing category, provider, or factory interface returns null.
     * Multiple providers without an explicit preference/default remain an error.
     *
     * @param aFactoryType specialized factory interface
     * @param aCategory requested category
     * @param aProviderIds ordered provider preferences, or null for default selection
     * @param <I> implementation contract
     * @param <C> context contract
     * @return matching factory, or null if none can be selected
     * @throws AIxCategoryProviderConflictException if multiple providers are eligible without a default
     */
    <I, C> AIiCategoryProviderFactory<I, C> findFactory(
            Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
            String aCategory, List<String> aProviderIds);

    /**
     * Lists all categories advertised by a specialized factory interface.
     *
     * @param aFactoryType specialized factory interface
     * @return immutable category set, empty if no factories are registered
     */
    Set<String> getAvailableCategories(Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType);

    /**
     * Lists registered provider identities for one specialized interface and category.
     *
     * @param aFactoryType specialized factory interface
     * @param aCategory category identifier
     * @return immutable provider set, empty when absent
     */
    Set<String> getAvailableProviderIds(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType, String aCategory);

    /**
     * Retrieves metadata for every registered category/provider pair of a factory SPI.
     * The data comes from one immutable registry snapshot and does not instantiate
     * implementation objects.
     *
     * @param aFactoryType specialized factory interface
     * @return immutable, deterministically ordered metadata list
     */
    List<AIiCategoryProviderFactoryMetadata> getAvailableFactoryMetadata(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType);

    /**
     * Tests category presence for a specialized factory interface.
     *
     * @param aFactoryType specialized factory interface
     * @param aCategory category identifier
     * @return true if a provider registered this category
     */
    default boolean hasCategory(Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType, String aCategory) {
        return !getAvailableProviderIds(aFactoryType, aCategory).isEmpty();
    }

    /**
     * Tests a specific registered category/provider combination.
     *
     * @param aFactoryType specialized factory interface
     * @param aCategory category identifier
     * @param aProviderId provider identifier
     * @return true if the provider implements the requested category
     */
    default boolean hasProvider(Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType,
                                String aCategory, String aProviderId) {
        return getAvailableProviderIds(aFactoryType, aCategory).contains(aProviderId);
    }

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
