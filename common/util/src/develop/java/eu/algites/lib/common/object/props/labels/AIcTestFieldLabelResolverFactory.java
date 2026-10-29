package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputFormat;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import java.util.Locale;

/**
 * Test-only CPDiscovery provider for fixed field-label resolution. The factory
 * is registered as a Java service and selected by the annotation's explicit
 * provider ID and test category, independently of production resolver providers.
 */
@AIaCategoryProviderFactory(providerId = "algites-field-label-test", categories = {"test-field-label"})
public final class AIcTestFieldLabelResolverFactory implements AIiFieldLabelResolverFactory {
    /**
     * Public no-argument constructor required by Java {@link java.util.ServiceLoader}.
     */
    public AIcTestFieldLabelResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override
    public Class<AIiFieldLabelResolver> getImplementationInterfaceType() {
        return AIiFieldLabelResolver.class;
    }

    /** {@inheritDoc} */
    @Override
    public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override
    public AIiFieldLabelResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"test-field-label".equals(aCategory)) {
            throw new IllegalArgumentException("Unsupported test field-label category: " + aCategory);
        }
        return new AIcConstantLabelResolver();
    }

    /**
     * Fixed resolver used to verify that CPDiscovery overrides static field metadata.
     */
    private static final class AIcConstantLabelResolver implements AIiFieldLabelResolver {
        /** {@inheritDoc} */
        @Override
        public String resolveLabel(Class<?> aOwnerClass, String aFieldName,
                AIiRenderingOutputPurpose aOutputPurpose, AIiRenderingOutputFormat aOutputFormat,
                Locale aOutputLocale) {
            return "ResolvedLabel";
        }
    }
}
