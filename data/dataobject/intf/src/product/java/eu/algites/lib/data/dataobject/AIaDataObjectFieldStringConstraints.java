package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema-independent string-field restrictions for a DataObject getter.
 * Constraints do not change raw presence or substitute defaults.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectFieldStringConstraints {
    /** @return minimum Unicode code-point length; -1 disables */
    int minLength() default -1;
    /** @return maximum Unicode code-point length; -1 disables */
    int maxLength() default -1;
    /** @return Java regular expression; empty disables */
    String pattern() default "";
    /** @return declared schema format identifier; empty when absent */
    String format() default "";
}
