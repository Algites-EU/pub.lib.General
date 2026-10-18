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
     * Declares whether the field is required, optional or forbidden on this contract.
     * @return the result of the operation
     */
    AInFieldPresenceKind presence() default AInFieldPresenceKind.ALLOWED;
    /**
     * Declares whether the field accepts an explicit null value.
     * @return the result of the operation
     */
    boolean allowsNull() default false;
    /** Name of a public static zero-argument factory on the getter's declaring interface. */
    String defaultFactoryMethod() default "";
    /**
     * Returns the representation-local name (XML QName local part where applicable).
     * @return the result of the operation
     */
    String localName() default "";
    /**
     * Returns the representation namespace or qualified schema scope.
     * @return the result of the operation
     */
    String namespace() default "";
    /**
     * Reports whether the field is represented as an XML attribute.
     * @return the result of the operation
     */
    boolean xmlAttribute() default false;
    /** Verify explicit, transitively declared OpenChoice participation on assignment. */
    boolean openChoice() default false;
}
