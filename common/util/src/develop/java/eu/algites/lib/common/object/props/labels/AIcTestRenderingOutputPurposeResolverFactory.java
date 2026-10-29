package eu.algites.lib.common.object.props.labels;

import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurpose;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolver;
import eu.algites.lib.common.object.rendering.AIiRenderingOutputPurposeResolverFactory;
import eu.algites.lib.common.object.rendering.AInRenderingOutputBuiltinPurpose;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;
import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

/**
 * Test-only CPDiscovery provider for a fixed USER rendering purpose. The
 * implementation is obtained through the specialized purpose resolver factory
 * SPI, not by reflective construction from the field annotation.
 */
@AIaCategoryProviderFactory(providerId = "algites-purpose-test", categories = {"test-purpose"})
public final class AIcTestRenderingOutputPurposeResolverFactory implements AIiRenderingOutputPurposeResolverFactory {
    /**
     * Public no-argument constructor required by Java {@link java.util.ServiceLoader}.
     */
    public AIcTestRenderingOutputPurposeResolverFactory() {
    }

    /** {@inheritDoc} */
    @Override
    public Class<AIiRenderingOutputPurposeResolver> getImplementationInterfaceType() {
        return AIiRenderingOutputPurposeResolver.class;
    }

    /** {@inheritDoc} */
    @Override
    public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override
    public AIiRenderingOutputPurposeResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        if (!"test-purpose".equals(aCategory)) {
            throw new IllegalArgumentException("Unsupported test purpose category: " + aCategory);
        }
        return new AIcUserPurposeResolver();
    }

    /**
     * Fixed-purpose resolver ensuring annotation mappings can use USER without
     * an explicit rendering purpose from the caller.
     */
    private static final class AIcUserPurposeResolver implements AIiRenderingOutputPurposeResolver {
        /** {@inheritDoc} */
        @Override
        public AIiRenderingOutputPurpose resolvePurpose() {
            return AInRenderingOutputBuiltinPurpose.USER;
        }
    }
}
