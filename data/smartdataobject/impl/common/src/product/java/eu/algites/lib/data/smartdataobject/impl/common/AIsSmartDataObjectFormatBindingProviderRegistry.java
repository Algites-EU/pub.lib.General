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
    private static final Map<ClassLoader, Boolean> EXTRA_LOADERS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<ModuleLayer, Boolean> EXTRA_LAYERS =
            Collections.synchronizedMap(new IdentityHashMap<>());

    private AIsSmartDataObjectFormatBindingProviderRegistry() { }

    /**
     * Adds a provider discovery classloader. Existing per-class metadata can
     * acquire this loader's additional provider contributions on next forType.
     *
     * @param aClassLoader non-null classloader exposing ServiceLoader registrations
     * @throws NullPointerException if aClassLoader is null
     */
    public static void registerClassLoader(ClassLoader aClassLoader) {
        EXTRA_LOADERS.put(Objects.requireNonNull(aClassLoader, "aClassLoader"), Boolean.TRUE);
    }

    /**
     * Stops discovering providers from an explicitly registered classloader.
     * Existing immutable class metadata are unaffected; any cached contribution
     * already resolved for that provider remains associated with its SDO class.
     *
     * @param aClassLoader classloader to remove, not null
     * @throws NullPointerException if the argument is null
     */
    public static void unregisterClassLoader(ClassLoader aClassLoader) {
        EXTRA_LOADERS.remove(Objects.requireNonNull(aClassLoader, "aClassLoader"));
    }

    /**
     * Adds one Java module layer to provider discovery. Unlike classloader-only
     * discovery this also discovers provider declarations from named modules.
     *
     * @param aLayer non-null module layer to include
     * @throws NullPointerException if the layer is null
     */
    public static void registerModuleLayer(ModuleLayer aLayer) {
        EXTRA_LAYERS.put(Objects.requireNonNull(aLayer, "aLayer"), Boolean.TRUE);
    }

    /**
     * Stops discovering providers from an explicitly registered module layer.
     * This does not invalidate contributions already attached to metadata.
     *
     * @param aLayer non-null previously registered module layer
     * @throws NullPointerException if the layer is null
     */
    public static void unregisterModuleLayer(ModuleLayer aLayer) {
        EXTRA_LAYERS.remove(Objects.requireNonNull(aLayer, "aLayer"));
    }

    /**
     * Discovers provider instances using the supplied, thread-context and
     * explicitly registered classloaders. Duplicate provider implementation
     * class identities are included only once.
     *
     * @param aImplementationLoader classloader of the SDO being inspected; nullable
     * @return immutable list of discovered providers
     */
    public static List<AIiSmartDataObjectFormatBindingProvider> getProviders(
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
            for (AIiSmartDataObjectFormatBindingProvider locProvider :
                    ServiceLoader.load(AIiSmartDataObjectFormatBindingProvider.class, locLoader)) {
                locProviders.putIfAbsent(locProvider.getClass(), locProvider);
            }
        }
        synchronized (EXTRA_LAYERS) {
            for (ModuleLayer locLayer : EXTRA_LAYERS.keySet()) {
                for (AIiSmartDataObjectFormatBindingProvider locProvider :
                        ServiceLoader.load(locLayer, AIiSmartDataObjectFormatBindingProvider.class)) {
                    locProviders.putIfAbsent(locProvider.getClass(), locProvider);
                }
            }
        }
        return List.copyOf(locProviders.values());
    }

    /**
     * Locates all providers available in the current thread's classloader scope.
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
