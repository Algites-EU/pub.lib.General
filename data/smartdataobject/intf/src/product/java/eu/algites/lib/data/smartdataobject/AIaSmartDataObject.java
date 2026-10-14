package eu.algites.lib.data.smartdataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** SmartDataObject-specific defaults for effective getter behavior. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaSmartDataObject {
    /** Permit an absent required field without a default to be read as null. */
    boolean allowNonPresentRead() default false;
}
