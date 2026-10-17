package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Normalized field metadata attached to a read-only data contract getter. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectField {
    /**
     * Returns the original, stable field name used by the data schema.
     * @return the result of the operation
     */
    String name() default "";
    /**
     * Returns the explanatory description of this declaration.
     * @return the result of the operation
     */
    String description() default "";
    /**
     * Reports whether a value must be present for this field.
     * @return the result of the operation
     */
    boolean presenceRequired() default false;
    /**
     * Declares whether the field accepts an explicit null value.
     * @return the result of the operation
     */
    boolean allowsNull() default false;
    /** Name of a public static zero-argument factory on the getter's declaring interface. */
    String defaultFactoryMethod() default "";
    /**
     * Returns the configured XML local name, independently of its namespace.
     * @return the result of the operation
     */
    String xmlLocalName() default "";
    /**
     * Returns the XML namespace associated with this schema or field.
     * @return the result of the operation
     */
    String xmlNamespace() default "";
    /**
     * Reports whether the field is represented as an XML attribute.
     * @return the result of the operation
     */
    boolean xmlAttribute() default false;
}
