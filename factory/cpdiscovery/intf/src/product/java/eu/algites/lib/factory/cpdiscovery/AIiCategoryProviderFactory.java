package eu.algites.lib.factory.cpdiscovery;

/** Typed creator for one provider, potentially supporting several categories. */
public interface AIiCategoryProviderFactory<I, C> {
    Class<I> getImplementationInterfaceType();
    Class<C> getContextInterfaceType();
    I createInstance(String aCategory, C aContext);
}
