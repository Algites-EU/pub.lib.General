package eu.algites.lib.common.object.rendering;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;

/**
 * Standard built-in resolver factory. Applications may register additional providers
 * or override the default provider selection through CategoryProviderDiscovery.
 */
@AIaCategoryProviderFactory(providerId = "algites-standard", categories = {"default"})
public final class AIcStandardRenderingOutputFormatResolverFactory implements AIiRenderingOutputFormatResolverFactory {
    /** Constructs the stateless standard factory for service discovery. */
    public AIcStandardRenderingOutputFormatResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override public Class<AIiRenderingOutputFormatResolver> getImplementationInterfaceType() { return AIiRenderingOutputFormatResolver.class; }

    /** {@inheritDoc} */
    @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override public AIiRenderingOutputFormatResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"default".equals(aCategory))
            throw new IllegalArgumentException("Unsupported resolver category: " + aCategory);
        return AIsRenderingOutputUtils::usedRenderingOutputFormat;
    }
}
