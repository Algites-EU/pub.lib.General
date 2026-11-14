package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema-independent list-field restrictions for a DataObject getter.
 * Constraints do not change raw presence or substitute defaults.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldListConstraints {
    /** @return minimum number of elements; -1 disables */
    int minItems() default -1;
    /** @return maximum number of elements; -1 disables */
    int maxItems() default -1;
    /** @return reject duplicate elements */
    boolean uniqueItems() default false;
}
