package eu.algites.lib.data.dataobject;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a resolver-defined effective value for an absent data-object property.
 * Defaults never make a raw value present and do not override an explicitly assigned null.
 * Each evaluation obtains its value from the configured category provider factory; this
 * permits immutable or fresh mutable defaults without reflective static method calls.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaFieldDefault {
    /**
     * Factory selection for the resolver responsible for the annotated field.
     * Its category is {@code default} when not explicitly specified.
     *
     * @return resolver factory selection
     */
    AIaCategoryProviderFactoryDefinition defaultValueResolver() default
            @AIaCategoryProviderFactoryDefinition(factoryClass = AIiFieldDefaultValueResolverFactory.class);
}
