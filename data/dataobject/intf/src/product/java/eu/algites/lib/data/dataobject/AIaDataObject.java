package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stable identity and descriptive metadata of a data-object schema.
 *
 * <p>SchemaId is the unversioned logical identifier, while schemaVersion selects a
 * version of that schema. Neither denotes the published SchemaUrl nor an object instance.
 * Generated mutable contracts carry a complete annotation even when their source
 * read-only interface specifies only partial metadata.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaDataObject {
    /** Unversioned logical SchemaId; empty only on incomplete handwritten source contracts. */
    String schemaId() default "";
    /** Positive SchemaVersion; -1 denotes an incomplete handwritten source contract. */
    int schemaVersion() default -1;
    /** Published URL of one schema representation; optional, never the SchemaId. */
    String schemaUrl() default "";
    String title() default "";
    String description() default "";
    String xmlLocalName() default "";
    String xmlNamespace() default "";
    String fingerprint() default "";
}
