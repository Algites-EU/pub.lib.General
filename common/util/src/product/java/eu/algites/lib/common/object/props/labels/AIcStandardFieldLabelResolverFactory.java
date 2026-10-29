package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;

/**
 * Standard built-in resolver factory. Applications may register additional providers
 * or override the default provider selection through CategoryProviderDiscovery.
 */
@AIaCategoryProviderFactory(providerId = "algites-standard", categories = {"default"})
public final class AIcStandardFieldLabelResolverFactory implements AIiFieldLabelResolverFactory {
    /** Constructs the stateless standard factory for service discovery. */
    public AIcStandardFieldLabelResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override public Class<AIiFieldLabelResolver> getImplementationInterfaceType() { return AIiFieldLabelResolver.class; }

    /** {@inheritDoc} */
    @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override public AIiFieldLabelResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"default".equals(aCategory))
            throw new IllegalArgumentException("Unsupported resolver category: " + aCategory);
        return new AIiFieldLabelResolver() {
            /** {@inheritDoc} */
            @Override public String resolveLabel(Class<?> aOwnerClass, String aFieldName,
                    eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose aPurpose,
                    eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat aFormat,
                    java.util.Locale aLocale) { return null; }
        };
    }
}
