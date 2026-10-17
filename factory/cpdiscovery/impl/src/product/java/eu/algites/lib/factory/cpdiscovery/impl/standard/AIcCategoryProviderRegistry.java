package eu.algites.lib.factory.cpdiscovery.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderRegistry;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderConflictException;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderDiscoveryException;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderUnavailableException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Atomic catalog of service-loader factories and provider defaults.
 *
 * Registration replaces immutable snapshots, so lookups do not acquire the write lock.
 * A removed source may still be referenced by a lookup already in progress.
 */
public final class AIcCategoryProviderRegistry implements AIiCategoryProviderRegistry {
    /**
     * Classpath resource containing default category-to-provider registrations.
     */
    public static final String DEFAULTS_RESOURCE =
        "META-INF/algites/category-provider-defaults.properties";

    private final ReentrantLock writeLock = new ReentrantLock();
    private final AtomicReference<Snapshot> current =
        new AtomicReference<>(new Snapshot(Map.of(), Map.of()));

    private record Key(Class<?> factoryInterface, String category, String providerId) { }
    private record Registration(AIiCategoryProviderFactory<?, ?> factory, String source) { }
    private record DefaultEntry(String providerId, String source) { }
    private record Resource(Object identity, String source, Map<String, String> values) { }
    private record Contribution(List<Registration> factories, List<Resource> resources) { }
    private record Snapshot(Map<Key, Registration> factories,
                            Map<String, DefaultEntry> defaults) { }

    private final Map<ClassLoader, Contribution> classLoaders = new IdentityHashMap<>();
    private final Map<ModuleLayer, Contribution> moduleLayers = new IdentityHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void registerClassLoaderFactories(ClassLoader aClassLoader) {
        Objects.requireNonNull(aClassLoader, "classLoader");
        writeLock.lock();
        try {
            if (classLoaders.containsKey(aClassLoader))
                throw new AIxCategoryProviderConflictException("ClassLoader already registered: " + aClassLoader);
            Contribution discovered = discoverClassLoader(aClassLoader);
            Map<ClassLoader, Contribution> loaders = new IdentityHashMap<>(classLoaders);
            loaders.put(aClassLoader, discovered);
            Snapshot candidate = aggregate(loaders, moduleLayers);
            classLoaders.put(aClassLoader, discovered);
            current.set(candidate);
        } finally { writeLock.unlock(); }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterClassLoaderFactories(ClassLoader aClassLoader) {
        Objects.requireNonNull(aClassLoader, "classLoader");
        writeLock.lock();
        try {
            if (!classLoaders.containsKey(aClassLoader))
                throw new AIxCategoryProviderUnavailableException("ClassLoader not registered: " + aClassLoader);
            Map<ClassLoader, Contribution> loaders = new IdentityHashMap<>(classLoaders);
            loaders.remove(aClassLoader);
            Snapshot candidate = aggregate(loaders, moduleLayers);
            classLoaders.remove(aClassLoader);
            current.set(candidate);
        } finally { writeLock.unlock(); }
    }

    /** {@inheritDoc} */
    @Override
    public void registerModuleLayerFactories(ModuleLayer aLayer) {
        Objects.requireNonNull(aLayer, "moduleLayer");
        writeLock.lock();
        try {
            if (moduleLayers.containsKey(aLayer))
                throw new AIxCategoryProviderConflictException("ModuleLayer already registered: " + aLayer);
            Contribution discovered = discoverLayer(aLayer);
            Map<ModuleLayer, Contribution> layers = new IdentityHashMap<>(moduleLayers);
            layers.put(aLayer, discovered);
            Snapshot candidate = aggregate(classLoaders, layers);
            moduleLayers.put(aLayer, discovered);
            current.set(candidate);
        } finally { writeLock.unlock(); }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterModuleLayerFactories(ModuleLayer aLayer) {
        Objects.requireNonNull(aLayer, "moduleLayer");
        writeLock.lock();
        try {
            if (!moduleLayers.containsKey(aLayer))
                throw new AIxCategoryProviderUnavailableException("ModuleLayer not registered: " + aLayer);
            Map<ModuleLayer, Contribution> layers = new IdentityHashMap<>(moduleLayers);
            layers.remove(aLayer);
            Snapshot candidate = aggregate(classLoaders, layers);
            moduleLayers.remove(aLayer);
            current.set(candidate);
        } finally { writeLock.unlock(); }
    }

    /** {@inheritDoc} */
    @Override
    public <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                                String aCategory, C aContext) {
        return getInstance(aFactoryType, aCategory, null, aContext);
    }

    /** {@inheritDoc} */
    @Override
    public <I, C> I getInstance(Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
                                String aCategory, String aProviderId, C aContext) {
        Objects.requireNonNull(aFactoryType, "factoryType");
        String category = requireName(aCategory, "category");
        Snapshot snapshot = current.get();
        String providerId = aProviderId;
        if (providerId == null) {
            DefaultEntry declared = snapshot.defaults.get(aFactoryType.getName() + "." + category);
            if (declared != null) providerId = declared.providerId();
            else {
                for (Key key : snapshot.factories.keySet()) {
                    if (key.factoryInterface() == aFactoryType && key.category().equals(category)) {
                        if (providerId != null)
                            throw new AIxCategoryProviderUnavailableException(
                                "Ambiguous provider for " + aFactoryType.getName() + "/" + category);
                        providerId = key.providerId();
                    }
                }
            }
        }
        if (providerId == null)
            throw new AIxCategoryProviderUnavailableException(
                "No provider for " + aFactoryType.getName() + "/" + category);
        Registration registered = snapshot.factories.get(new Key(aFactoryType, category, providerId));
        if (registered == null)
            throw new AIxCategoryProviderUnavailableException(
                "Unavailable provider " + providerId + " for " + aFactoryType.getName() + "/" + category);
        AIiCategoryProviderFactory<?, ?> factory = registered.factory();
        if (aContext != null && !factory.getContextInterfaceType().isInstance(aContext))
            throw new AIxCategoryProviderDiscoveryException("Invalid context type for " + registered.source());
        if (!aFactoryType.isInstance(factory))
            throw new AIxCategoryProviderDiscoveryException("Factory type mismatch: " + registered.source());
        try {
            @SuppressWarnings("unchecked")
            AIiCategoryProviderFactory<I, C> typed = (AIiCategoryProviderFactory<I, C>) factory;
            I instance = typed.createInstance(category, aContext);
            if (instance == null || !typed.getImplementationInterfaceType().isInstance(instance))
                throw new AIxCategoryProviderDiscoveryException("Factory returned incorrect instance: " + registered.source());
            return instance;
        } catch (AIxCategoryProviderDiscoveryException error) { throw error; }
        catch (RuntimeException error) {
            throw new AIxCategoryProviderDiscoveryException("Factory failed: " + registered.source(), error);
        }
    }

    private static Contribution discoverClassLoader(ClassLoader loader) {
        try {
            List<Registration> factories = new ArrayList<>();
            for (AIiCategoryProviderFactory<?, ?> factory :
                    ServiceLoader.load(AIiCategoryProviderFactory.class, loader))
                factories.add(new Registration(factory, factory.getClass().getName()));
            List<Resource> resources = new ArrayList<>();
            Enumeration<URL> urls = loader.getResources(DEFAULTS_RESOURCE);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream stream = url.openStream()) {
                    resources.add(new Resource(url.toExternalForm(), url.toExternalForm(), loadProperties(stream)));
                }
            }
            return new Contribution(List.copyOf(factories), List.copyOf(resources));
        } catch (IOException | java.util.ServiceConfigurationError error) {
            throw new AIxCategoryProviderDiscoveryException("Failed to discover ClassLoader providers", error);
        }
    }

    private static Contribution discoverLayer(ModuleLayer layer) {
        try {
            List<Registration> factories = new ArrayList<>();
            for (AIiCategoryProviderFactory<?, ?> factory :
                    ServiceLoader.load(layer, AIiCategoryProviderFactory.class))
                factories.add(new Registration(factory, factory.getClass().getName()));
            List<Resource> resources = new ArrayList<>();
            for (ModuleLayer scope : layerAndParents(layer)) {
                for (Module module : scope.modules()) {
                    try (InputStream stream = module.getResourceAsStream(DEFAULTS_RESOURCE)) {
                        if (stream != null) resources.add(new Resource(
                            module, module.toString(), loadProperties(stream)));
                    }
                }
            }
            return new Contribution(List.copyOf(factories), List.copyOf(resources));
        } catch (IOException | java.util.ServiceConfigurationError error) {
            throw new AIxCategoryProviderDiscoveryException("Failed to discover ModuleLayer providers", error);
        }
    }

    private static Set<ModuleLayer> layerAndParents(ModuleLayer layer) {
        Set<ModuleLayer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        walkLayers(layer, visited);
        return visited;
    }

    private static void walkLayers(ModuleLayer layer, Set<ModuleLayer> visited) {
        if (visited.add(layer)) for (ModuleLayer parent : layer.parents()) walkLayers(parent, visited);
    }

    private static Map<String, String> loadProperties(InputStream input) throws IOException {
        Properties properties = new Properties() {
            /** {@inheritDoc} */
            @Override
            public synchronized Object put(Object key, Object value) {
                if (containsKey(key))
                    throw new AIxCategoryProviderConflictException("Duplicate property in one resource: " + key);
                return super.put(key, value);
            }
        };
        properties.load(input);
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String value = requireName(properties.getProperty(key), "default providerId");
            values.put(requireName(key, "default key"), value);
        }
        return Map.copyOf(values);
    }

    private static Snapshot aggregate(Map<ClassLoader, Contribution> loaders,
                                      Map<ModuleLayer, Contribution> layers) {
        Map<Key, Registration> discovered = new LinkedHashMap<>();
        Map<String, DefaultEntry> defaultsByName = new LinkedHashMap<>();
        Set<Class<?>> encounteredFactoryClasses = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Object> encounteredResourceIds = new HashSet<>();
        List<Contribution> all = new ArrayList<>(loaders.values());
        all.addAll(layers.values());
        for (Contribution contribution : all) {
            for (Resource resource : contribution.resources()) {
                if (!encounteredResourceIds.add(resource.identity())) continue;
                for (var entry : resource.values().entrySet()) {
                    DefaultEntry old = defaultsByName.putIfAbsent(entry.getKey(),
                        new DefaultEntry(entry.getValue(), resource.source()));
                    if (old != null && !old.providerId().equals(entry.getValue()))
                        throw new AIxCategoryProviderConflictException(
                            "Conflicting defaults for " + entry.getKey() + ": " + old.source() + " and " + resource.source());
                }
            }
            for (Registration registered : contribution.factories()) {
                Class<?> type = registered.factory().getClass();
                if (!encounteredFactoryClasses.add(type)) continue;
                AIaCategoryProviderFactory annotation = type.getAnnotation(AIaCategoryProviderFactory.class);
                if (annotation == null)
                    throw new AIxCategoryProviderDiscoveryException("Missing @AIaCategoryProviderFactory: " + type);
                String providerId = requireName(annotation.providerId(), "providerId");
                Class<?> specialization = resolveSpecialization(type);
                if (registered.factory().getImplementationInterfaceType() == null ||
                    registered.factory().getContextInterfaceType() == null)
                    throw new AIxCategoryProviderDiscoveryException("Missing factory type metadata: " + type);
                if (!registered.factory().getContextInterfaceType().isInterface())
                    throw new AIxCategoryProviderDiscoveryException("Factory context must be an interface: " + type);
                if (annotation.categories().length == 0)
                    throw new AIxCategoryProviderDiscoveryException("Factory declares no categories: " + type);
                for (String category : annotation.categories()) {
                    Key key = new Key(specialization, requireName(category, "category"), providerId);
                    Registration old = discovered.putIfAbsent(key, registered);
                    if (old != null)
                        throw new AIxCategoryProviderConflictException("Duplicate factory " + key + ": " + old.source() + " and " + registered.source());
                }
            }
        }
        return new Snapshot(Map.copyOf(discovered), Map.copyOf(defaultsByName));
    }

    private static Class<?> resolveSpecialization(Class<?> type) {
        Set<Class<?>> candidates = new LinkedHashSet<>();
        findInterfaces(type, candidates);
        candidates.remove(AIiCategoryProviderFactory.class);
        Set<Class<?>> lessSpecific = new HashSet<>();
        for (Class<?> a : candidates)
            for (Class<?> b : candidates)
                if (a != b && a.isAssignableFrom(b)) lessSpecific.add(a);
        candidates.removeAll(lessSpecific);
        if (candidates.size() != 1)
            throw new AIxCategoryProviderDiscoveryException(
                "Factory must implement exactly one specialized factory SPI: " + type + " -> " + candidates);
        return candidates.iterator().next();
    }

    private static void findInterfaces(Class<?> type, Set<Class<?>> destination) {
        if (type == null || type == Object.class) return;
        for (Class<?> iface : type.getInterfaces()) {
            if (AIiCategoryProviderFactory.class.isAssignableFrom(iface)) {
                destination.add(iface);
                findInterfaces(iface, destination);
            }
        }
        findInterfaces(type.getSuperclass(), destination);
    }

    private static String requireName(String value, String label) {
        if (value == null || value.isBlank() || !value.trim().equals(value))
            throw new AIxCategoryProviderDiscoveryException("Invalid " + label + ": " + value);
        return value;
    }
}
