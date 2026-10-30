package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Runtime carrier of multiple independent {@link AIaXmlKeyDefinition} index
 * declarations attached to the same repeated XML field.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaXmlKeyDefinitions {
    /**
     * @return ordered, nonempty set of key constraints for one XML list
     */
    AIaXmlKeyDefinition[] value();
}
