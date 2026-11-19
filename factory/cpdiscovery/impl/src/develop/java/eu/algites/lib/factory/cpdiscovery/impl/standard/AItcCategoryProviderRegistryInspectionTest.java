package eu.algites.lib.factory.cpdiscovery.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactoryMetadata;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderUnavailableException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Verifies provider metadata inspection, ordered fallback and nullable factory selection. */
public final class AItcCategoryProviderRegistryInspectionTest {
    /** Service returned by the test factory. */
    public interface AIiTestService {
        /** @return the selected provider's ID */
        String providerId();
    }

    /** Context contract required by the test factory. */
    public interface AIiTestContext { }

    /** Specialized SPI under test. */
    public interface AIiTestFactory extends AIiCategoryProviderFactory<AIiTestService, AIiTestContext> { }

    /** First test provider, implementing two categories. */
    @AIaCategoryProviderFactory(providerId = "provider-a", categories = {"execute:source_generation", "bind:product_api"})
    public static final class AIcProviderA implements AIiTestFactory {
        /** Public ServiceLoader constructor. */
        public AIcProviderA() { }
        /** {@inheritDoc} */
        @Override public Class<AIiTestService> getImplementationInterfaceType() { return AIiTestService.class; }
        /** {@inheritDoc} */
        @Override public Class<AIiTestContext> getContextInterfaceType() { return AIiTestContext.class; }
        /** {@inheritDoc} */
        @Override public AIiTestService createInstance(String category, AIiTestContext context) { return () -> "provider-a"; }
    }

    /** Second test provider, supporting only the executable category. */
    @AIaCategoryProviderFactory(providerId = "provider-b", categories = {"execute:source_generation"})
    public static final class AIcProviderB implements AIiTestFactory {
        /** Public ServiceLoader constructor. */
        public AIcProviderB() { }
        /** {@inheritDoc} */
        @Override public Class<AIiTestService> getImplementationInterfaceType() { return AIiTestService.class; }
        /** {@inheritDoc} */
        @Override public Class<AIiTestContext> getContextInterfaceType() { return AIiTestContext.class; }
        /** {@inheritDoc} */
        @Override public AIiTestService createInstance(String category, AIiTestContext context) { return () -> "provider-b"; }
    }

    /**
     * Creates two registered providers with a default, then checks introspection,
     * preference override and removal without calling createInstance during lookup.
     *
     * @throws Exception if the temporary ServiceLoader resources cannot be prepared
     */
    @Test
    public void testMetadataOrderedFallbackAndUnregistration() throws Exception {
        Path root = Files.createTempDirectory("cpdiscovery-inspection-");
        Path services = root.resolve("META-INF/services/eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory");
        Path defaults = root.resolve(AIcCategoryProviderRegistry.DEFAULTS_RESOURCE);
        Files.createDirectories(services.getParent());
        Files.createDirectories(defaults.getParent());
        Files.writeString(services, AIcProviderA.class.getName() + "\n" + AIcProviderB.class.getName() + "\n");
        Files.writeString(defaults, AIiTestFactory.class.getName() + ".execute\\:source_generation=provider-b\n");
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{root.toUri().toURL()}, getClass().getClassLoader())) {
            AIcCategoryProviderRegistry registry = new AIcCategoryProviderRegistry();
            registry.registerClassLoaderFactories(loader);
            Assert.assertEquals(registry.getAvailableCategories(AIiTestFactory.class),
                    Set.of("execute:source_generation", "bind:product_api"));
            Assert.assertEquals(registry.getAvailableProviderIds(AIiTestFactory.class, "execute:source_generation"),
                    Set.of("provider-a", "provider-b"));
            Assert.assertTrue(registry.hasProvider(AIiTestFactory.class, "bind:product_api", "provider-a"));
            Assert.assertFalse(registry.hasProvider(AIiTestFactory.class, "bind:product_api", "provider-b"));
            Assert.assertNull(registry.findFactory(AIiTestFactory.class, "unavailable", List.of("provider-a")));
            Assert.assertNull(registry.findFactory(AIiTestFactory.class, "execute:source_generation", List.of("missing")));
            Assert.assertEquals(registry.findFactory(AIiTestFactory.class, "execute:source_generation",
                    List.of("missing", "provider-a", "provider-b")).createInstance("execute:source_generation", null).providerId(),
                    "provider-a");
            Assert.assertEquals(registry.getInstance(AIiTestFactory.class, "execute:source_generation", null).providerId(),
                    "provider-b");
            List<AIiCategoryProviderFactoryMetadata> metadata = registry.getAvailableFactoryMetadata(AIiTestFactory.class);
            Assert.assertEquals(metadata.size(), 3);
            Assert.assertTrue(metadata.get(0) instanceof AIcdCategoryProviderFactoryMetadata);
            Assert.assertEquals(metadata.get(0).category(), "bind:product_api");
            Assert.assertEquals(metadata.stream().filter(AIiCategoryProviderFactoryMetadata::defaultProvider).count(), 1L);
            Assert.assertEquals(metadata.get(0).implementationInterfaceType(), AIiTestService.class);
            Assert.expectThrows(UnsupportedOperationException.class, () -> metadata.clear());
            registry.unregisterClassLoaderFactories(loader);
            Assert.assertFalse(registry.hasCategory(AIiTestFactory.class, "execute:source_generation"));
            Assert.assertNull(registry.findFactory(AIiTestFactory.class, "execute:source_generation", null));
            Assert.expectThrows(AIxCategoryProviderUnavailableException.class,
                    () -> registry.getInstance(AIiTestFactory.class, "execute:source_generation", null));
        } finally {
            try (var entries = Files.walk(root)) {
                for (Path entry : entries.sorted(java.util.Comparator.reverseOrder()).toList())
                    Files.deleteIfExists(entry);
            }
        }
    }
}
