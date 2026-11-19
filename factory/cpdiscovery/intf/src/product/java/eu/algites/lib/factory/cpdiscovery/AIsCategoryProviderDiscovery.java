package eu.algites.lib.factory.cpdiscovery;

import java.util.Objects;
import java.util.List;
import java.util.Set;
import java.util.ServiceLoader;

/**
 * Entry point for resolving annotation-declared provider factories through a single
 * service-discovered, {@link AIiCategoryProviderRegistry}.
 * The registry owns ClassLoader/ModuleLayer registration and its immutable snapshots;
 * this utility never instantiates resolver implementation classes reflectively.
 */
public final class AIsCategoryProviderDiscovery {
    private static volatile AIiCategoryProviderRegistry registry;

    private AIsCategoryProviderDiscovery() {
    }

    /**
     * Returns the application registry; when first called, discovers one
     * through ServiceLoader and registers the current context classloader as a factory scope.
     * An absent or ambiguous registry fails explicitly instead of silently ignoring resolvers.
     *
     * @return selected registry
     * @throws AIxCategoryProviderUnavailableException if no registry implementation is available
     * @throws AIxCategoryProviderConflictException if more than one registry is discovered
     */
    public static AIiCategoryProviderRegistry getRegistry() {
        AIiCategoryProviderRegistry locRegistry = registry;
        if (locRegistry != null) return locRegistry;
        synchronized (AIsCategoryProviderDiscovery.class) {
            locRegistry = registry;
            if (locRegistry != null) return locRegistry;
            AIiCategoryProviderRegistry locFound = null;
            for (AIiCategoryProviderRegistry locCandidate : ServiceLoader.load(AIiCategoryProviderRegistry.class)) {
                if (locFound != null)
                    throw new AIxCategoryProviderConflictException("Multiple CategoryProvider registries found");
                locFound = locCandidate;
            }
            if (locFound == null)
                throw new AIxCategoryProviderUnavailableException("No CategoryProvider registry available");
            ClassLoader locLoader = Thread.currentThread().getContextClassLoader();
            if (locLoader == null) locLoader = AIsCategoryProviderDiscovery.class.getClassLoader();
            if (locLoader != null) locFound.registerClassLoaderFactories(locLoader);
            registry = locFound;
            return locFound;
        }
    }

    /**
     * Creates an implementation using the annotation-defined specialized factory and
     * the registry's category/provider resolution. The caller determines the default
     * category and supplies a context understood by the specialized factory SPI.
     *
     * @param aDefinition selection attached to the annotation member
     * @param aRequiredFactoryType expected specialized factory SPI
     * @param aDefaultCategory category used when the annotation category is empty
     * @param aContext factory context
     * @param <I> returned implementation type
     * @param <C> factory context type
     * @return implementation selected by CPDiscovery
     * @throws IllegalArgumentException if the annotation selects an incompatible factory SPI
     */
    public static <I, C> I getInstance(AIaCategoryProviderFactoryDefinition aDefinition,
            Class<? extends AIiCategoryProviderFactory<I, C>> aRequiredFactoryType,
            String aDefaultCategory, C aContext) {
        Objects.requireNonNull(aDefinition, "definition");
        Objects.requireNonNull(aRequiredFactoryType, "requiredFactoryType");
        if (!aDefinition.factoryInterface().equals(aRequiredFactoryType))
            throw new IllegalArgumentException("Unexpected factory interface: " + aDefinition.factoryInterface());
        String locCategory = aDefinition.factoryCategory().isBlank()
                ? aDefaultCategory : aDefinition.factoryCategory();
        String locProviderId = aDefinition.factoryProviderId().isBlank()
                ? null : aDefinition.factoryProviderId();
        return getRegistry().getInstance(aRequiredFactoryType, locCategory, locProviderId, aContext);
    }
    /**
     * Discovers the first registered provider matching ordered preferences, without
     * constructing a provider instance. A missing match returns null.
     *
     * @param aFactoryType specialized factory SPI
     * @param aCategory category identifier
     * @param aProviderIds preferred provider IDs in priority order, or null for default selection
     * @param <I> produced implementation type
     * @param <C> context contract
     * @return registered factory, or null if unavailable
     */
    public static <I, C> AIiCategoryProviderFactory<I, C> findFactory(
            Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
            String aCategory, List<String> aProviderIds) {
        return getRegistry().findFactory(aFactoryType, aCategory, aProviderIds);
    }

    /**
     * Lists the available categories for a specialized factory SPI.
     *
     * @param aFactoryType specialized factory SPI
     * @return immutable available categories
     */
    public static Set<String> getAvailableCategories(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType) {
        return getRegistry().getAvailableCategories(aFactoryType);
    }

    /**
     * Lists providers registered for a specialized SPI and category.
     *
     * @param aFactoryType specialized factory SPI
     * @param aCategory category identifier
     * @return immutable provider identities
     */
    public static Set<String> getAvailableProviderIds(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType, String aCategory) {
        return getRegistry().getAvailableProviderIds(aFactoryType, aCategory);
    }

    /**
     * Returns provider metadata without creating implementation instances.
     *
     * @param aFactoryType specialized factory SPI
     * @return immutable metadata, sorted by category and provider ID
     */
    public static List<AIiCategoryProviderFactoryMetadata> getAvailableFactoryMetadata(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType) {
        return getRegistry().getAvailableFactoryMetadata(aFactoryType);
    }

}
