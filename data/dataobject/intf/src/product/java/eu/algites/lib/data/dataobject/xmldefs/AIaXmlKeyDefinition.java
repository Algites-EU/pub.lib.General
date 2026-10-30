package eu.algites.lib.data.dataobject.xmldefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one XSD {@code xs:key} enforced on a direct repeated XML element field.
 * Every path is a dot-separated sequence of canonical XML field names; the
 * ordered paths form one (possibly compound) unique key. All components are
 * mandatory, and the unique index is local to the containing XML object.
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
     * @return ordered, nonempty paths of mandatory scalar key components
     */
    String[] fields();
}
