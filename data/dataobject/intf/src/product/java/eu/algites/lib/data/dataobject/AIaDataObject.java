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
     * Names the optional JSON/YAML instance-document property receiving {@link #schemaUrl()}.
     * The name is supplied by the selected source-structure naming convention, not inferred
     * from the Java type or the schema dialect keyword {@code $schema}. The empty default
     * disables automatic emission; when configured, the runtime omits absent URL values.
     *
     * @return exact serialized property name, or empty when URL metadata is disabled
     */
    String dataSourceSchemaUrlField() default "";
    /**
     * Names the optional instance-document property receiving {@link #schemaId()}.
     * The logical identifier remains available in this annotation even when the target
     * property is disabled. The corresponding schema-source identifier key must be
     * configured for a nonempty target name; no identifier is derived from the URL.
     *
     * @return serialized logical-identifier property name, or empty to omit it
     */
    String dataSourceSchemaIdentifierField() default "";
    /**
     * Names the optional instance-document property receiving {@link #schemaVersion()}.
     * A nonempty name requires a configured source-schema version key. The empty default
     * suppresses serialization, and the sentinel version {@code -1} is never emitted.
     *
     * @return serialized schema-version property name, or empty to omit it
     */
    String dataSourceSchemaVersionField() default "";
    /** Exact data-document property that receives the schema URL; empty prevents automatic emission. */
    /** Exact data-document property that receives the logical schema identifier; empty prevents emission. */
    /** Exact data-document property that receives the schema version; empty prevents emission. */
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

    /**
     * Declares the inherited setter parameter-type strategy for this data object.
     *
     * <p>{@code DEFAULT} delegates to the configured generator default. A resolved
     * generated contract records an effective strategy independent of the
     * generating build's environment. A fixed strategy does not prevent Java
     * method inheritance; it prevents unsafe type narrowing in descendants.
     *
     * @return the setter parameter inheritance strategy, or {@code DEFAULT}
     *         when this declaration leaves the selection to its context
     */
    AInFieldSetterInheritancePolicy setterInheritancePolicy()
            default AInFieldSetterInheritancePolicy.DEFAULT;

}
