package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime carrier of multiple independent {@link AIaDataObjectListKeyDefinition} index
 * declarations attached to the same repeated field.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectListKeyDefinitions {
    /**
     * @return ordered, nonempty set of key constraints for one list
     */
    AIaDataObjectListKeyDefinition[] value();
}
