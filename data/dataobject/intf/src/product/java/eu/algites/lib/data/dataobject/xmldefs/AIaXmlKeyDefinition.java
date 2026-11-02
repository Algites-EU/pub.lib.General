package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one XSD {@code xs:key} or {@code xs:unique} constraint on a direct
 * repeated XML element field.
 * Every path is a dot-separated sequence of canonical XML field names.
 * A required constraint ({@code xs:key}) needs all fields, whereas an optional
 * constraint ({@code xs:unique}) permits incomplete keys. Both enforce
 * uniqueness for complete key tuples only. The index is local to the container.
 *
 * <p>These paths are resolved against typed generated object getters at runtime,
 * not evaluated using XML XPath against serialized documents. Descendant
 * selectors spanning several collections are intentionally not represented.</p>
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(AIaXmlKeyDefinitions.class)
public @interface AIaXmlKeyDefinition {
    /**
     * @return nonblank identity constraint name unique among this field's indexes
     */
    String name();

    /**
     * @return ordered, nonempty paths of scalar key components; a complete
     *         value is mandatory for {@code xs:key} but optional for {@code xs:unique}
     */
    String[] fields();

    /**
     * @return true for {@code xs:key}; false for {@code xs:unique}, which permits
     *         missing components to be grouped under a null lookup key
     */
    boolean required() default true;
}
