package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema-independent numeric-field restrictions for a DataObject getter.
 * Constraints do not change raw presence or substitute defaults.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldNumericConstraints {
    /** @return inclusive decimal lower bound; empty disables */
    String minimum() default "";
    /** @return inclusive decimal upper bound; empty disables */
    String maximum() default "";
    /** @return exclude the lower bound */
    boolean exclusiveMinimum() default false;
    /** @return exclude the upper bound */
    boolean exclusiveMaximum() default false;
    /** @return positive decimal factor; empty disables */
    String multipleOf() default "";
    /** @return declared numeric format identifier; empty when absent */
    String format() default "";
}
