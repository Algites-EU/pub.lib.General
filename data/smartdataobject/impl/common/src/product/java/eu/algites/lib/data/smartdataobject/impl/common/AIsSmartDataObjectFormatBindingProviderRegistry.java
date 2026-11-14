package eu.algites.lib.data.smartdataobject.impl.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * ServiceLoader-backed discovery of format-binding implementations.
 * Providers are general services; only concrete SDO implementation bindings
 * are selected separately by each application-scoped binding registry.
 *
 * @author linhart1
 */
public final class AIsSmartDataObjectFormatBindingProviderRegistry {
    /** Immutable discovery snapshot; published after successful initialization. */
    private static volatile List<AIiSmartDataObjectFormatBindingProvider> CACHED_PROVIDERS;

    private static final Map<ClassLoader, Boolean> EXTRA_LOADERS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<ModuleLayer, Boolean> EXTRA_LAYERS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AIsSmartDataObjectFormatBindingProviderRegistry() { }

    /**
     * Adds a provider discovery classloader before the first discovery.
     * Discovery is immutable for the lifetime of this registry classloader;
     * registration after initialization does not trigger another scan.
     *
     * @param aClassLoader non-null classloader exposing ServiceLoader registrations
     * @throws NullPointerException if aClassLoader is null
     */
    public static void registerClassLoader(ClassLoader aClassLoader) {
        EXTRA_LOADERS.put(Objects.requireNonNull(aClassLoader, "aClassLoader"), Boolean.TRUE);
    }

    /**
     * Stops registering a classloader for future first discovery.
     * A completed discovery snapshot is intentionally not invalidated.
     *
     * @param aClassLoader classloader to remove, not null
     * @throws NullPointerException if the argument is null
     */
    public static void unregisterClassLoader(ClassLoader aClassLoader) {
        EXTRA_LOADERS.remove(Objects.requireNonNull(aClassLoader, "aClassLoader"));
    }

    /**
     * Adds one Java module layer before first discovery, including providers
     * declared in named modules. A cached discovery snapshot is not invalidated.
     *
     * @param aLayer non-null module layer to include
     * @throws NullPointerException if the layer is null
     */
    public static void registerModuleLayer(ModuleLayer aLayer) {
        EXTRA_LAYERS.put(Objects.requireNonNull(aLayer, "aLayer"), Boolean.TRUE);
    }

    /**
     * Removes a layer from the scopes of a not-yet-completed discovery.
     * A cached discovery snapshot is not invalidated.
     *
     * @param aLayer non-null previously registered module layer
     * @throws NullPointerException if the layer is null
     */
    public static void unregisterModuleLayer(ModuleLayer aLayer) {
        EXTRA_LAYERS.remove(Objects.requireNonNull(aLayer, "aLayer"));
    }

    /**
     * Discovers provider instances once using the supplied implementation loader,
     * thread-context loader, registered loaders and module layers. Each provider
     * implementation class is instantiated at most once per registry lifetime.
     * Every subsequent caller receives the identical immutable provider list.
     * Register additional discovery scopes before the first call.
     *
     * @param aImplementationLoader classloader of the SDO being inspected; nullable
     * @return immutable list of discovered providers
     */
    public static List<AIiSmartDataObjectFormatBindingProvider> getProviders(
            ClassLoader aImplementationLoader) {
        List<AIiSmartDataObjectFormatBindingProvider> locCached = CACHED_PROVIDERS;
        if (locCached != null) return locCached;
        synchronized (AIsSmartDataObjectFormatBindingProviderRegistry.class) {
            locCached = CACHED_PROVIDERS;
            if (locCached == null) {
                locCached = discover(aImplementationLoader);
                CACHED_PROVIDERS = locCached;
            }
        }
        return locCached;
    }

    private static List<AIiSmartDataObjectFormatBindingProvider> discover(
            ClassLoader aImplementationLoader) {
        List<ClassLoader> locLoaders = new ArrayList<>();
        addUnique(locLoaders, aImplementationLoader);
        addUnique(locLoaders, Thread.currentThread().getContextClassLoader());
        addUnique(locLoaders, AIiSmartDataObjectFormatBindingProvider.class.getClassLoader());
        synchronized (EXTRA_LOADERS) {
            for (ClassLoader locLoader : EXTRA_LOADERS.keySet()) addUnique(locLoaders, locLoader);
        }
        Map<Class<?>, AIiSmartDataObjectFormatBindingProvider> locProviders = new LinkedHashMap<>();
        for (ClassLoader locLoader : locLoaders) {
            ServiceLoader.load(AIiSmartDataObjectFormatBindingProvider.class, locLoader)
                    .stream().forEach(aEntry -> locProviders.computeIfAbsent(
                            aEntry.type(), aIgnored -> aEntry.get()));
        }
        synchronized (EXTRA_LAYERS) {
            for (ModuleLayer locLayer : EXTRA_LAYERS.keySet()) {
                ServiceLoader.load(locLayer, AIiSmartDataObjectFormatBindingProvider.class)
                        .stream().forEach(aEntry -> locProviders.computeIfAbsent(
                                aEntry.type(), aIgnored -> aEntry.get()));
            }
        }
        return List.copyOf(locProviders.values());
    }

    /**
     * Returns the process-cached discovery result, using the current thread's
     * classloader only for the first initialization.
     *
     * @return immutable discovered providers
     */
    public static List<AIiSmartDataObjectFormatBindingProvider> getProviders() {
        return getProviders(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Finds all providers with a matching stable format code.
     *
     * @param aFormatId external format code
     * @return zero or more providers, without implicit selection
     */
    public static List<AIiSmartDataObjectFormatBindingProvider> getProviders(String aFormatId) {
        Objects.requireNonNull(aFormatId, "aFormatId");
        return getProviders().stream().filter(aProvider -> aFormatId.equals(aProvider.getFormatId())).toList();
    }

    private static void addUnique(List<ClassLoader> aLoaders, ClassLoader aLoader) {
        if (aLoader == null) return;
        for (ClassLoader locExisting : aLoaders) if (locExisting == aLoader) return;
        aLoaders.add(aLoader);
    }
}
