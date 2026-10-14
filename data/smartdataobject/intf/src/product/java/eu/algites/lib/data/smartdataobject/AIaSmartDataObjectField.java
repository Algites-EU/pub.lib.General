package eu.algites.lib.data.smartdataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Per-getter override of the SmartDataObject effective-reading policy. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaSmartDataObjectField {
    /** Overrides the interface-level default, including an explicit false. */
    boolean allowNonPresentRead() default false;
}
