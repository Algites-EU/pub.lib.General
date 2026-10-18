package eu.algites.lib.common.object.rendering;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;

/**
 * Standard built-in resolver factory. Applications may register additional providers
 * or override the default provider selection through CategoryProviderDiscovery.
 */
@AIaCategoryProviderFactory(providerId = "algites-standard", categories = {"default"})
public final class AIcStandardRenderingOutputLocaleResolverFactory implements AIiRenderingOutputLocaleResolverFactory {
    /** Constructs the stateless standard factory for service discovery. */
    public AIcStandardRenderingOutputLocaleResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override public Class<AIiRenderingOutputLocaleResolver> getImplementationInterfaceType() { return AIiRenderingOutputLocaleResolver.class; }

    /** {@inheritDoc} */
    @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override public AIiRenderingOutputLocaleResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"default".equals(aCategory))
            throw new IllegalArgumentException("Unsupported resolver category: " + aCategory);
        return AIsRenderingOutputUtils::usedRenderingOutputLocale;
    }
}
