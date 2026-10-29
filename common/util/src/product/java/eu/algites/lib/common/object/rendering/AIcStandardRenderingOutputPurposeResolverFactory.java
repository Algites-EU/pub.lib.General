package eu.algites.lib.common.object.rendering;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;

/**
 * Standard built-in resolver factory. Applications may register additional providers
 * or override the default provider selection through CategoryProviderDiscovery.
 */
@AIaCategoryProviderFactory(providerId = "algites-standard", categories = {"default"})
public final class AIcStandardRenderingOutputPurposeResolverFactory implements AIiRenderingOutputPurposeResolverFactory {
    /** Constructs the stateless standard factory for service discovery. */
    public AIcStandardRenderingOutputPurposeResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override public Class<AIiRenderingOutputPurposeResolver> getImplementationInterfaceType() { return AIiRenderingOutputPurposeResolver.class; }

    /** {@inheritDoc} */
    @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override public AIiRenderingOutputPurposeResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"default".equals(aCategory))
            throw new IllegalArgumentException("Unsupported resolver category: " + aCategory);
        return new AIcDefaultRenderingOutputPurposeResolver();
    }
}
