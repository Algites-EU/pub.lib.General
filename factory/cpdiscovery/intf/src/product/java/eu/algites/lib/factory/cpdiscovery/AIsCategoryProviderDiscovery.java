package eu.algites.lib.factory.cpdiscovery;

import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Entry point for resolving annotation-declared provider factories through a single
 * explicitly configured, or service-discovered, {@link AIiCategoryProviderRegistry}.
 * The registry owns ClassLoader/ModuleLayer registration and its immutable snapshots;
 * this utility never instantiates resolver implementation classes reflectively.
 */
public final class AIsCategoryProviderDiscovery {
    private static volatile AIiCategoryProviderRegistry registry;

    private AIsCategoryProviderDiscovery() {
    }

    /**
     * Installs the application-scoped registry, for instance after registering the desired
     * module layers or classloaders. A null value clears the override and permits service
     * discovery. Callers should install it during startup before resolving field metadata.
     *
     * @param aRegistry application registry, or null to restore service lookup
     */
    public static void setRegistry(AIiCategoryProviderRegistry aRegistry) {
        registry = aRegistry;
    }

    /**
     * Returns the application registry; when none is explicitly installed, discovers one
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
        if (!aDefinition.factoryClass().equals(aRequiredFactoryType))
            throw new IllegalArgumentException("Unexpected factory interface: " + aDefinition.factoryClass());
        String locCategory = aDefinition.factoryCategory().isBlank()
                ? aDefaultCategory : aDefinition.factoryCategory();
        String locProviderId = aDefinition.factoryProviderId().isBlank()
                ? null : aDefinition.factoryProviderId();
        return getRegistry().getInstance(aRequiredFactoryType, locCategory, locProviderId, aContext);
    }
}
