package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime carrier of multiple independent {@link AIaKeyDefinition} index
 * declarations attached to the same repeated field.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaKeyDefinitions {
    /**
     * @return ordered, nonempty set of key constraints for one list
     */
    AIaKeyDefinition[] value();
}
