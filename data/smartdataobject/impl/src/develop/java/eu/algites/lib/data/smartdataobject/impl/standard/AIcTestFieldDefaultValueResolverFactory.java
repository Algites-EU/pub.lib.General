package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;

import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolver;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolverFactory;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactory;

/** Test-only CPDiscovery provider exercising resolver-backed scalar and object defaults. */
@AIaCategoryProviderFactory(providerId = "test-values", categories = {"default"})
public final class AIcTestFieldDefaultValueResolverFactory implements AIiFieldDefaultValueResolverFactory {
    /** Allows ServiceLoader instantiation during the test suite. */
    public AIcTestFieldDefaultValueResolverFactory() { }

    /** {@inheritDoc} */
    @Override public Class<AIiFieldDefaultValueResolver> getImplementationInterfaceType() {
        return AIiFieldDefaultValueResolver.class;
    }

    /** {@inheritDoc} */
    @Override public Class<AIiCategoryProviderResolutionContext> getContextInterfaceType() {
        return AIiCategoryProviderResolutionContext.class;
    }

    /** {@inheritDoc} */
    @Override public AIiFieldDefaultValueResolver createInstance(String aCategory, AIiCategoryProviderResolutionContext aContext) {
        return (aOwner, aGetter) -> switch (aGetter.getName()) {
            case "getPort" -> Integer.valueOf(8080);
            case "getDefaultRead" -> "default";
            case "getOptions" -> new AItcSmartDataObjectTest.AIigOptions() {
                /** {@inheritDoc} */
                @Override public String getHost() { return "localhost"; }
                /** {@inheritDoc} */
                @Override public Boolean getSecure() { return Boolean.TRUE; }
            };
            default -> throw new IllegalArgumentException("Unknown test default field: " + aGetter);
        };
    }
}
