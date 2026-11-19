package eu.algites.lib.factory.cpdiscovery.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactoryMetadata;
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
    private final AtomicReference<AIcdSnapshot> current =
        new AtomicReference<>(new AIcdSnapshot(Map.of(), Map.of()));

    private record AIcdKey(Class<?> factoryInterface, String category, String providerId) { }
    private record AIcdRegistration(AIiCategoryProviderFactory<?, ?> factory, String source) { }
    private record AIcdDefaultEntry(String providerId, String source) { }
    private record AIcdResource(Object identity, String source, Map<String, String> values) { }
    private record AIcdContribution(List<AIcdRegistration> factories, List<AIcdResource> resources) { }
    private record AIcdSnapshot(Map<AIcdKey, AIcdRegistration> factories,
                               Map<String, AIcdDefaultEntry> defaults) { }

    private final Map<ClassLoader, AIcdContribution> classLoaders = new IdentityHashMap<>();
    private final Map<ModuleLayer, AIcdContribution> moduleLayers = new IdentityHashMap<>();

    /** {@inheritDoc} */
    @Override
    public void registerClassLoaderFactories(ClassLoader aClassLoader) {
        Objects.requireNonNull(aClassLoader, "classLoader");
        writeLock.lock();
        try {
            if (classLoaders.containsKey(aClassLoader))
                throw new AIxCategoryProviderConflictException("ClassLoader already registered: " + aClassLoader);
            AIcdContribution discovered = discoverClassLoader(aClassLoader);
            Map<ClassLoader, AIcdContribution> loaders = new IdentityHashMap<>(classLoaders);
            loaders.put(aClassLoader, discovered);
            AIcdSnapshot candidate = aggregate(loaders, moduleLayers);
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
            Map<ClassLoader, AIcdContribution> loaders = new IdentityHashMap<>(classLoaders);
            loaders.remove(aClassLoader);
            AIcdSnapshot candidate = aggregate(loaders, moduleLayers);
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
            AIcdContribution discovered = discoverLayer(aLayer);
            Map<ModuleLayer, AIcdContribution> layers = new IdentityHashMap<>(moduleLayers);
            layers.put(aLayer, discovered);
            AIcdSnapshot candidate = aggregate(classLoaders, layers);
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
            Map<ModuleLayer, AIcdContribution> layers = new IdentityHashMap<>(moduleLayers);
            layers.remove(aLayer);
            AIcdSnapshot candidate = aggregate(classLoaders, layers);
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
        AIiCategoryProviderFactory<I, C> selected = aProviderId == null
                ? findFactory(aFactoryType, category, null)
                : findFactory(aFactoryType, category, List.of(requireName(aProviderId, "providerId")));
        if (selected == null)
            throw new AIxCategoryProviderUnavailableException(
                "No matching provider for " + aFactoryType.getName() + "/" + category +
                (aProviderId == null ? "" : "/" + aProviderId));
        String source = selected.getClass().getName();
        AIiCategoryProviderFactory<?, ?> factory = selected;
        if (aContext != null && !factory.getContextInterfaceType().isInstance(aContext))
            throw new AIxCategoryProviderDiscoveryException("Invalid context type for " + source);
        if (!aFactoryType.isInstance(factory))
            throw new AIxCategoryProviderDiscoveryException("Factory type mismatch: " + source);
        try {
            @SuppressWarnings("unchecked")
            AIiCategoryProviderFactory<I, C> typed = (AIiCategoryProviderFactory<I, C>) factory;
            I instance = typed.createInstance(category, aContext);
            if (instance == null || !typed.getImplementationInterfaceType().isInstance(instance))
                throw new AIxCategoryProviderDiscoveryException("Factory returned incorrect instance: " + source);
            return instance;
        } catch (AIxCategoryProviderDiscoveryException error) { throw error; }
        catch (RuntimeException error) {
            throw new AIxCategoryProviderDiscoveryException("Factory failed: " + source, error);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <I, C> AIiCategoryProviderFactory<I, C> findFactory(
            Class<? extends AIiCategoryProviderFactory<I, C>> aFactoryType,
            String aCategory, List<String> aProviderIds) {
        Objects.requireNonNull(aFactoryType, "factoryType");
        String category = requireName(aCategory, "category");
        AIcdSnapshot snapshot = current.get();
        if (aProviderIds != null && !aProviderIds.isEmpty()) {
            for (String providerId : aProviderIds) {
                String validId = requireName(providerId, "providerId");
                AIcdRegistration match = snapshot.factories.get(new AIcdKey(aFactoryType, category, validId));
                if (match != null) return castFactory(aFactoryType, match);
            }
            return null;
        }
        AIcdDefaultEntry configured = snapshot.defaults.get(aFactoryType.getName() + "." + category);
        if (configured != null) {
            AIcdRegistration match = snapshot.factories.get(new AIcdKey(aFactoryType, category, configured.providerId()));
            return match == null ? null : castFactory(aFactoryType, match);
        }
        AIcdRegistration selected = null;
        for (var entry : snapshot.factories.entrySet()) {
            if (entry.getKey().factoryInterface() != aFactoryType || !entry.getKey().category().equals(category))
                continue;
            if (selected != null)
                throw new AIxCategoryProviderUnavailableException(
                    "Ambiguous provider for " + aFactoryType.getName() + "/" + category +
                    "; select an explicit provider or configure a default");
            selected = entry.getValue();
        }
        return selected == null ? null : castFactory(aFactoryType, selected);
    }

    private static <I, C> AIiCategoryProviderFactory<I, C> castFactory(
            Class<? extends AIiCategoryProviderFactory<I, C>> type, AIcdRegistration registration) {
        if (!type.isInstance(registration.factory()))
            throw new AIxCategoryProviderDiscoveryException("Factory type mismatch: " + registration.source());
        @SuppressWarnings("unchecked")
        AIiCategoryProviderFactory<I, C> result = (AIiCategoryProviderFactory<I, C>) registration.factory();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getAvailableCategories(Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType) {
        Objects.requireNonNull(aFactoryType, "factoryType");
        Set<String> result = new java.util.TreeSet<>();
        for (AIcdKey key : current.get().factories.keySet())
            if (key.factoryInterface() == aFactoryType) result.add(key.category());
        return Collections.unmodifiableSet(result);
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getAvailableProviderIds(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType, String aCategory) {
        Objects.requireNonNull(aFactoryType, "factoryType");
        String category = requireName(aCategory, "category");
        Set<String> result = new java.util.TreeSet<>();
        for (AIcdKey key : current.get().factories.keySet())
            if (key.factoryInterface() == aFactoryType && key.category().equals(category))
                result.add(key.providerId());
        return Collections.unmodifiableSet(result);
    }

    /** {@inheritDoc} */
    @Override
    public List<AIiCategoryProviderFactoryMetadata> getAvailableFactoryMetadata(
            Class<? extends AIiCategoryProviderFactory<?, ?>> aFactoryType) {
        Objects.requireNonNull(aFactoryType, "factoryType");
        AIcdSnapshot snapshot = current.get();
        List<AIiCategoryProviderFactoryMetadata> result = new ArrayList<>();
        for (var entry : snapshot.factories.entrySet()) {
            AIcdKey key = entry.getKey();
            if (key.factoryInterface() != aFactoryType) continue;
            AIiCategoryProviderFactory<?, ?> factory = entry.getValue().factory();
            AIcdDefaultEntry configured = snapshot.defaults.get(aFactoryType.getName() + "." + key.category());
            result.add(new AIcdCategoryProviderFactoryMetadata(
                    key.category(), key.providerId(), factory.getImplementationInterfaceType(),
                    factory.getContextInterfaceType(), configured != null && configured.providerId().equals(key.providerId()),
                    entry.getValue().source()));
        }
        result.sort(java.util.Comparator.comparing(AIiCategoryProviderFactoryMetadata::category)
                .thenComparing(AIiCategoryProviderFactoryMetadata::providerId));
        return List.copyOf(result);
    }

    private static AIcdContribution discoverClassLoader(ClassLoader loader) {
        try {
            List<AIcdRegistration> factories = new ArrayList<>();
            for (AIiCategoryProviderFactory<?, ?> factory :
                    ServiceLoader.load(AIiCategoryProviderFactory.class, loader))
                factories.add(new AIcdRegistration(factory, factory.getClass().getName()));
            List<AIcdResource> resources = new ArrayList<>();
            Enumeration<URL> urls = loader.getResources(DEFAULTS_RESOURCE);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream stream = url.openStream()) {
                    resources.add(new AIcdResource(url.toExternalForm(), url.toExternalForm(), loadProperties(stream)));
                }
            }
            return new AIcdContribution(List.copyOf(factories), List.copyOf(resources));
        } catch (IOException | java.util.ServiceConfigurationError error) {
            throw new AIxCategoryProviderDiscoveryException("Failed to discover ClassLoader providers", error);
        }
    }

    private static AIcdContribution discoverLayer(ModuleLayer layer) {
        try {
            List<AIcdRegistration> factories = new ArrayList<>();
            for (AIiCategoryProviderFactory<?, ?> factory :
                    ServiceLoader.load(layer, AIiCategoryProviderFactory.class))
                factories.add(new AIcdRegistration(factory, factory.getClass().getName()));
            List<AIcdResource> resources = new ArrayList<>();
            for (ModuleLayer scope : layerAndParents(layer)) {
                for (Module module : scope.modules()) {
                    try (InputStream stream = module.getResourceAsStream(DEFAULTS_RESOURCE)) {
                        if (stream != null) resources.add(new AIcdResource(
                            module, module.toString(), loadProperties(stream)));
                    }
                }
            }
            return new AIcdContribution(List.copyOf(factories), List.copyOf(resources));
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

    private static AIcdSnapshot aggregate(Map<ClassLoader, AIcdContribution> loaders,
                                      Map<ModuleLayer, AIcdContribution> layers) {
        Map<AIcdKey, AIcdRegistration> discovered = new LinkedHashMap<>();
        Map<String, AIcdDefaultEntry> defaultsByName = new LinkedHashMap<>();
        Set<Class<?>> encounteredFactoryClasses = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Object> encounteredResourceIds = new HashSet<>();
        List<AIcdContribution> all = new ArrayList<>(loaders.values());
        all.addAll(layers.values());
        for (AIcdContribution contribution : all) {
            for (AIcdResource resource : contribution.resources()) {
                if (!encounteredResourceIds.add(resource.identity())) continue;
                for (var entry : resource.values().entrySet()) {
                    AIcdDefaultEntry old = defaultsByName.putIfAbsent(entry.getKey(),
                        new AIcdDefaultEntry(entry.getValue(), resource.source()));
                    if (old != null && !old.providerId().equals(entry.getValue()))
                        throw new AIxCategoryProviderConflictException(
                            "Conflicting defaults for " + entry.getKey() + ": " + old.source() + " and " + resource.source());
                }
            }
            for (AIcdRegistration registered : contribution.factories()) {
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
                    AIcdKey key = new AIcdKey(specialization, requireName(category, "category"), providerId);
                    AIcdRegistration old = discovered.putIfAbsent(key, registered);
                    if (old != null)
                        throw new AIxCategoryProviderConflictException("Duplicate factory " + key + ": " + old.source() + " and " + registered.source());
                }
            }
        }
        return new AIcdSnapshot(Map.copyOf(discovered), Map.copyOf(defaultsByName));
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
