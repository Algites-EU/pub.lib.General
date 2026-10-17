package eu.algites.lib.factory.cpdiscovery.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderConflictException;
import eu.algites.lib.factory.cpdiscovery.AIxCategoryProviderUnavailableException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/** Verifies the native Java ServiceLoader discovery and atomic unregister behavior. */
public class AItcCategoryProviderRegistryTest {
    public interface AIiTestService {
        String getValue();
    }

    public interface AIiTestContext { }

    public interface AIiTestFactory extends AIiCategoryProviderFactory<AIiTestService, AIiTestContext> { }

    public static final class AIcTestContext implements AIiTestContext { }

    @AIaCategoryProviderFactory(providerId = "eu.algites.test", categories = {"sample", "second"})
    public static final class AIcTestFactory implements AIiTestFactory {
        public AIcTestFactory() { }

        @Override
        public Class<AIiTestService> getImplementationInterfaceType() {
            return AIiTestService.class;
        }

        @Override
        public Class<AIiTestContext> getContextInterfaceType() {
            return AIiTestContext.class;
        }

        @Override
        public AIiTestService createInstance(String aCategory, AIiTestContext aContext) {
            return () -> aCategory;
        }
    }

    @Test
    public void testServiceLoaderRegistrationAndRemoval() throws Exception {
        Path locRoot = Files.createTempDirectory("algites-cpdiscovery-test-");
        Path locServices = locRoot.resolve("META-INF/services/eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory");
        Files.createDirectories(locServices.getParent());
        Files.writeString(locServices, AIcTestFactory.class.getName() + "\n");
        Path locDefaults = locRoot.resolve("META-INF/algites/category-provider-defaults.properties");
        Files.createDirectories(locDefaults.getParent());
        Files.writeString(locDefaults, AIiTestFactory.class.getName() + ".sample=eu.algites.test\n");
        try (URLClassLoader locLoader = new URLClassLoader(
                new java.net.URL[]{locRoot.toUri().toURL()}, getClass().getClassLoader())) {
            AIcCategoryProviderRegistry locRegistry = new AIcCategoryProviderRegistry();
            locRegistry.registerClassLoaderFactories(locLoader);
            Assert.assertEquals(locRegistry.getInstance(AIiTestFactory.class, "sample", new AIcTestContext()).getValue(), "sample");
            Assert.assertEquals(locRegistry.getInstance(AIiTestFactory.class, "second", "eu.algites.test", new AIcTestContext()).getValue(), "second");
            Assert.expectThrows(AIxCategoryProviderConflictException.class,
                    () -> locRegistry.registerClassLoaderFactories(locLoader));
            locRegistry.unregisterClassLoaderFactories(locLoader);
            Assert.expectThrows(AIxCategoryProviderUnavailableException.class,
                    () -> locRegistry.getInstance(AIiTestFactory.class, "sample", new AIcTestContext()));
        } finally {
            Files.deleteIfExists(locDefaults);
            Files.deleteIfExists(locDefaults.getParent());
            Files.deleteIfExists(locServices);
            Files.deleteIfExists(locServices.getParent());
            Files.deleteIfExists(locServices.getParent().getParent());
            Files.deleteIfExists(locRoot.resolve("META-INF"));
            Files.deleteIfExists(locRoot);
        }
    }
}
