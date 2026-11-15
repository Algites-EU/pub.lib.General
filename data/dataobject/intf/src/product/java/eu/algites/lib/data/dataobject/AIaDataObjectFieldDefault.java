package eu.algites.lib.data.dataobject;

import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional, independent getter declaration of an effective or constrained default.
 * Its presence means that a default exists, including when the factory returns
 * {@code Optional.empty()} (an explicit null default).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldDefault {
    /** @return default application policy */
    AInDataObjectFieldDefaultKind kind() default AInDataObjectFieldDefaultKind.DECLARATION_ONLY;

    /** @return CPDiscovery selection, identified by a specialized factory SPI */
    AIaCategoryProviderFactoryDefinition defaultValueFactory();

    /** @return immutable lexical configuration forwarded to the selected factory */
    String[] defaultArguments() default {};
}
