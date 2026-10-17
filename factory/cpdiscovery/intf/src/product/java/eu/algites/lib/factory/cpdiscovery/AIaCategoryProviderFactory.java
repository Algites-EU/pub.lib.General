package eu.algites.lib.factory.cpdiscovery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares provider identity and the categories served by an implementation factory. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaCategoryProviderFactory {
    String providerId();
    String[] categories();
}
