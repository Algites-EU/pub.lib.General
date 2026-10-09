package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Normalized field metadata attached to a read-only data contract getter. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectField {
    String name();
    String description() default "";
    boolean presenceRequired() default false;
    boolean allowsNull() default false;
    /** Name of a public static zero-argument factory on the getter's declaring interface. */
    String defaultFactoryMethod() default "";
    String xmlLocalName() default "";
    String xmlNamespace() default "";
    boolean xmlAttribute() default false;
}
