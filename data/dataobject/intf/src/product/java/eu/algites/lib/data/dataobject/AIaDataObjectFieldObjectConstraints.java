package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema-independent object-field restrictions for a DataObject getter.
 * Constraints do not change raw presence or substitute defaults.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldObjectConstraints {
    /** @return minimum number of properties; -1 disables */
    int minProperties() default -1;
    /** @return maximum number of properties; -1 disables */
    int maxProperties() default -1;
}
