package eu.algites.lib.factory.cpdiscovery;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Identifies a configured factory selection on another annotation member.
 * A factory interface is declared rather than an implementation: the registry selects
 * its implementation by category and, optionally, by a stable provider identifier.
 * An empty category or provider ID delegates selection to the consumer's documented
 * default category and to registry provider-default/unique-provider resolution.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AIaCategoryProviderFactoryDefinition {
    /**
     * Specialized {@link AIiCategoryProviderFactory} interface implemented by discovered factories.
     *
     * @return interface used as the lookup key, never a resolver implementation class
     */
    Class<? extends AIiCategoryProviderFactory<?, ?>> factoryClass();

    /**
     * Optional category override; empty selects the consuming resolver's default category.
     *
     * @return factory category or an empty string for the consuming default
     */
    String factoryCategory() default "";

    /**
     * Optional explicit factory provider; empty allows a registry default or unique provider.
     *
     * @return provider identifier or an empty string for automatic selection
     */
    String factoryProviderId() default "";
}
