package eu.algites.lib.data.dataobject;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;

/** Dedicated CPDiscovery factory SPI for data-object default-value resolvers. */
public interface AIiFieldDefaultValueResolverFactory
        extends AIiCategoryProviderFactory<AIiFieldDefaultValueResolver, AIiCategoryProviderResolutionContext> {
}
