package eu.algites.lib.data.dataobject.jsondefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JSON/YAML source schema identity and optional instance-document metadata.
 *
 * <p>These properties belong to the JSON schema representation,
 * never to a schema-neutral DataObject or an individual naming convention.
 * A blank target field name suppresses its emission to instance documents.
 *
 * @author linhart1
 */
/** @deprecated use {@link eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema}; legacy contracts remain supported during migration. */
@Deprecated(since = "1.1", forRemoval = false)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaJsonDataObject {
    /** @return unversioned schema identifier, or empty for incomplete source contracts */
    String schemaId() default "";
    /** @return positive source schema version, or -1 when unspecified */
    int schemaVersion() default -1;
    /** @return published URL of the schema representation, or empty when unspecified */
    String schemaUrl() default "";
    /** @return instance-property name for schema URL, or empty to disable serialization */
    String dataSourceSchemaUrlField() default "";
    /** @return instance-property name for schema ID, or empty to disable serialization */
    String dataSourceSchemaIdentifierField() default "";
    /** @return instance-property name for schema version, or empty to disable serialization */
    String dataSourceSchemaVersionField() default "";
}
