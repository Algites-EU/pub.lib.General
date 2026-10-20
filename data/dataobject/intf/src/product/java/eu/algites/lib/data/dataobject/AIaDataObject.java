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
    /**
     * Returns the human-readable title of the data object schema.
     * @return the result of the operation
     */
    String title() default "";
    /**
     * Returns the explanatory description of this declaration.
     * @return the result of the operation
     */
    String description() default "";
    /**
     * Returns the schema fingerprint used to identify a particular definition.
     * @return the result of the operation
     */
    String fingerprint() default "";
    /**
     * Identifies the naming convention used for the annotated contract's own type name.
     *
     * <p>The value is an opaque catalog identifier. Its syntax and versioning policy
     * belong to the owner of the naming catalog, not to this annotation or the generators.
     * Empty indicates a manually authored contract without an explicit naming convention.
     *
     * @return the exact naming convention identifier, or an empty string when unspecified
     */
    String namingConvention() default "";
}
