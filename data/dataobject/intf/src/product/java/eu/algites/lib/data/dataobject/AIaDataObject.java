package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema-independent declaration metadata of a data-object contract.
 *
 * Schema identifiers and instance-document metadata are defined only by the
 * corresponding JSON/YAML source-technology annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaDataObject {
    /**
     * Returns the human-readable title of the data object schema.
     * @return the result of the operation
     */
    String title() default "";
    /**
     * Returns the explanatory description of this declaration.
     * @return the result of the operation
     */
    String description() default "";
    /**
     * Declares the inherited setter parameter-type strategy for this data object.
     *
     * <p>{@code DEFAULT} delegates to the configured generator default. A resolved
     * generated contract records an effective strategy independent of the
     * generating build's environment. A fixed strategy does not prevent Java
     * method inheritance; it prevents unsafe type narrowing in descendants.
     *
     * @return the setter parameter inheritance strategy, or {@code DEFAULT}
     *         when this declaration leaves the selection to its context
     */
    AInFieldSetterInheritancePolicy setterInheritancePolicy()
            default AInFieldSetterInheritancePolicy.DEFAULT;

}
