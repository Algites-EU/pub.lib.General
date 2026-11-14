package eu.algites.lib.data.smartdataobject.impl.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Overrides the concrete implementation selected when unmarshalling a nested SDO field.
 * The nearest declared annotation in the getter override hierarchy wins.
 * @author linhart1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaSmartDataObjectFieldImplementationBinding {
    /** @return concrete SDO implementation for this field */
    Class<? extends AIcSmartDataObject> implementationClass();
}
