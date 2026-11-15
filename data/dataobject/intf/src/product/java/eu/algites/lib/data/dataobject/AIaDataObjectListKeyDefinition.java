package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one named typed index on a repeated DataObject field. XML
 * required {@code xs:key}, optional {@code xs:unique}, and JSON/YAML virtual
 * map keys share the
 * same contract.
 * Every path is a dot-separated sequence of logical DataObject field names.
 * A required constraint needs all fields, whereas an optional
 * constraint permits incomplete keys. Both enforce
 * uniqueness for complete key tuples only. The index is local to the container.
 *
 * <p>These paths are resolved against typed generated object getters at runtime,
 * not evaluated using serialization-specific XPath against serialized documents. Descendant
 * selectors spanning several collections are intentionally not represented.</p>
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(AIaDataObjectListKeyDefinitions.class)
public @interface AIaDataObjectListKeyDefinition {
    /**
     * @return nonblank identity constraint name unique among this field's indexes
     */
    String name();

    /**
     * @return ordered, nonempty paths of scalar key components; a complete
     *         value is mandatory for required keys but optional for uniqueness constraints
     */
    String[] fields();

    /**
     * Selects a direct repeating child of the referenced object TYPE when this
     * annotation is attached to the getter for that object. The empty selector
     * denotes an index declared directly on the annotated list getter.
     * This reference-specific metadata never changes the shared object TYPE.
     *
     * @return direct repeated child name or empty for a directly indexed list
     */
    String selector() default "";

    /**
     * @return true for a required key; false for an optional uniqueness constraint, which permits
     *         missing components to be grouped under a null lookup key
     */
    boolean required() default true;
}
