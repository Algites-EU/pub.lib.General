package eu.algites.lib.factory.cpdiscovery;

/** Typed creator for one provider, potentially supporting several categories. */
public interface AIiCategoryProviderFactory<I, C> {
    /**
     * Returns the implementation interface type.
     * @return the implementation interface type
     */
    Class<I> getImplementationInterfaceType();
    /**
     * Returns the context interface type.
     * @return the context interface type
     */
    Class<C> getContextInterfaceType();
    /**
     * Creates instance using the supplied inputs.
     * @param aCategory category of the required provider
     * @param aContext context used to create or select an implementation
     * @return the create instance
     */
    I createInstance(String aCategory, C aContext);
}
