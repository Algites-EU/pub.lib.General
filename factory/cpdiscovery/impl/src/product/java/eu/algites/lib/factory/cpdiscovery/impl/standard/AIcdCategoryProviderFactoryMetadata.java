package eu.algites.lib.factory.cpdiscovery.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactoryMetadata;

import java.util.Objects;

/**
 * Immutable discovery metadata for one registered specialized factory and category.
 * Provider identifiers describe implementations, never artifact coordinates or usage IDs.
 * Metadata inspection does not create a provider instance.
 *
 * @param category supported category identifier
 * @param providerId stable provider identifier
 * @param implementationInterfaceType interface of values produced by the factory
 * @param contextInterfaceType context contract accepted by the factory
 * @param defaultProvider whether this provider is explicitly configured as the category default
 * @param source discovery source used for diagnostics
 */
public record AIcdCategoryProviderFactoryMetadata(
        String category,
        String providerId,
        Class<?> implementationInterfaceType,
        Class<?> contextInterfaceType,
        boolean defaultProvider,
        String source) implements AIiCategoryProviderFactoryMetadata {
    /**
     * Validates the immutable metadata of a registered provider.
     *
     * @throws NullPointerException if any mandatory metadata field is missing
     */
    public AIcdCategoryProviderFactoryMetadata {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(providerId, "providerId");
        Objects.requireNonNull(implementationInterfaceType, "implementationInterfaceType");
        Objects.requireNonNull(contextInterfaceType, "contextInterfaceType");
        Objects.requireNonNull(source, "source");
    }
}
