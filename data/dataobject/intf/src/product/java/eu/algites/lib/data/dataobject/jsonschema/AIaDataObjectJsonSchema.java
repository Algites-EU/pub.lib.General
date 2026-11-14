package eu.algites.lib.data.dataobject.jsonschema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schema identity and instance-document metadata for DataObjects defined using JSON Schema.
 * Used by both JSONDEFS and YAMLDEFS, independently of their wire serialization.
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaDataObjectJsonSchema {
    /** @return unversioned source schema identifier, or empty when unspecified */
    String schemaId() default "";
    /** @return positive schema version, or -1 when unspecified */
    int schemaVersion() default -1;
    /** @return published schema URL, or empty when unspecified */
    String schemaUrl() default "";
    /** @return source-document field carrying the schema URL, or empty if disabled */
    String dataSourceSchemaUrlField() default "";
    /** @return source-document field carrying the schema identifier, or empty if disabled */
    String dataSourceSchemaIdentifierField() default "";
    /** @return source-document field carrying the schema version, or empty if disabled */
    String dataSourceSchemaVersionField() default "";
}
