package eu.algites.lib.data.dataobject;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderResolutionContext;
import java.lang.reflect.Type;

/** Inputs supplied by SDO metadata when resolving a declared default. */
public interface AIiFieldDefaultValueFactoryContext extends AIiCategoryProviderResolutionContext {
    /** @return actual field type, including generic parameters where available */
    Type getExpectedType();

    /** @return a defensive copy of the annotation's factory arguments */
    String[] getDefaultArguments();
}
