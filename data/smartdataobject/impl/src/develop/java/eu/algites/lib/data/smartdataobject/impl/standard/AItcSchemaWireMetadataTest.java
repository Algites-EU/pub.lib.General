package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import org.testng.annotations.Test;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Exercises schema-source metadata emission at the SDO serialization boundary.
 * A logical schema ID is not a substitute for a representation-specific URL;
 * optional target fields determine exactly which metadata enter wire documents.
 */
public final class AItcSchemaWireMetadataTest {
    /** Contract with the Algites default: only the schema URL is serialized. */
    @AIaDataObject(schemaId = "test.contract", schemaVersion = 7,
            schemaUrl = "https://example.invalid/schema_7.jsondef.schema.json",
            dataSourceSchemaUrlField = "$schema")
    public interface AIigUrlOnly extends AIiDataObject {
        /** Return the explicitly present scalar value, if provided. */
        @AIaDataObjectField(fieldName = "Value")
        String getValue();
    }

    /** Contract explicitly configured to emit ID and version in addition to the URL. */
    @AIaDataObject(schemaId = "test.contract", schemaVersion = 7,
            schemaUrl = "https://example.invalid/schema_7.jsondef.schema.json",
            dataSourceSchemaUrlField = "$schema",
            dataSourceSchemaIdentifierField = "$schemaId",
            dataSourceSchemaVersionField = "$schemaVersion")
    public interface AIigAllMetadata extends AIiDataObject {
        /** Return the contract value without generating identity metadata from it. */
        @AIaDataObjectField(fieldName = "Value")
        String getValue();
    }

    /** Mutable instance of the default contract. */
    public static final class AIcgdUrlOnly extends AIcSmartDataObject implements AIigUrlOnly {
        /** Create an initially empty instance. */
        public AIcgdUrlOnly() { super(); }
        /** {@inheritDoc} */
        @Override public String getValue() { return (String) get_RawField("Value"); }
    }

    /** Mutable instance whose contract enables the three output fields. */
    public static final class AIcgdAllMetadata extends AIcSmartDataObject implements AIigAllMetadata {
        /** Create an initially empty instance. */
        public AIcgdAllMetadata() { super(); }
        /** {@inheritDoc} */
        @Override public String getValue() { return (String) get_RawField("Value"); }
    }

    /** Contract declaring the same system property explicitly as an ordinary field. */
    @AIaDataObject(schemaId = "test.contract", schemaVersion = 7,
            schemaUrl = "https://example.invalid/schema_7.jsondef.schema.json",
            dataSourceSchemaUrlField = "$schema")
    public interface AIigExplicitSchemaUrl extends AIiDataObject {
        /** The schema URL is both declared in the source schema and supplied by metadata. */
        @AIaDataObjectField(fieldName = "$schema")
        String getSchemaUrl();
    }

    /** Mutable instance of a contract with an explicitly declared metadata field. */
    public static final class AIcgdExplicitSchemaUrl extends AIcSmartDataObject implements AIigExplicitSchemaUrl {
        /** Constructs an empty object; its schema URL is supplied by the annotation. */
        public AIcgdExplicitSchemaUrl() { super(); }
        /** {@inheritDoc} */
        @Override public String getSchemaUrl() { return (String) get_RawField("$schema"); }
        /** Sets a manually supplied schema URL for conflict testing. */
        public void setSchemaUrl(String aValue) { set_RawField("$schema", aValue); }
    }

    /** A declared property sharing the configured system field is serialized exactly once. */
    @Test public void explicitSchemaPropertyIsMergedWithMetadata() {
        var locObject = new AIcgdExplicitSchemaUrl();
        String locUrl = "https://example.invalid/schema_7.jsondef.schema.json";
        String locText = locObject.toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES);
        assertTrue(locText.contains("\"$schema\":\"" + locUrl + "\""));
        assertTrue(locText.indexOf("$schema") == locText.lastIndexOf("$schema"));
        locObject.setSchemaUrl("https://different.invalid/");
        expectThrows(IllegalArgumentException.class, () -> locObject.toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES));
    }

    /** Contract that also declares the system metadata name as an ordinary field. */
    @AIaDataObject(schemaId = "test.contract", schemaVersion = 7,
            schemaUrl = "https://example.invalid/schema_7.jsondef.schema.json",
            dataSourceSchemaUrlField = "$schema")
    public interface AIigDeclaredSchemaProperty extends AIiDataObject {
        /** Return the explicitly stored URL field when one has been assigned. */
        @AIaDataObjectField(fieldName = "$schema")
        String getSchemaUrl();
    }

    /** Mutable instance for checking that metadata and schema-declared fields coexist. */
    public static final class AIcgdDeclaredSchemaProperty extends AIcSmartDataObject
            implements AIigDeclaredSchemaProperty {
        /** Create an initially empty instance. */
        public AIcgdDeclaredSchemaProperty() { super(); }
        /** {@inheritDoc} */
        @Override public String getSchemaUrl() { return (String) get_RawField("$schema"); }
    }

    /** Checks that Algites writes only the configured URL, without guessing ID or version keys. */
    @Test public void defaultMappingEmitsOnlySchemaUrl() {
        String locText = new AIcgdUrlOnly().toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES);
        assertTrue(locText.contains("\"$schema\":\"https://example.invalid/schema_7.jsondef.schema.json\""));
        assertFalse(locText.contains("$schemaId"));
        assertFalse(locText.contains("$schemaVersion"));
    }

    /** Validates that a source-declared metadata field is not duplicated or silently overwritten. */
    @Test public void declaredMetadataPropertyMustAgreeWithContract() {
        var locDocument = new AIcgdDeclaredSchemaProperty();
        String locExpected = "\"$schema\":\"https://example.invalid/schema_7.jsondef.schema.json\"";
        String locText = locDocument.toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES);
        assertTrue(locText.contains(locExpected));
        assertTrue(locText.indexOf("$schema") == locText.lastIndexOf("$schema"));
        locDocument.set_RawField("$schema", "https://example.invalid/wrong.json");
        expectThrows(IllegalArgumentException.class, () -> locDocument.toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES));
    }

    /** Checks that non-default target names are honored independently of the URL field. */
    @Test public void explicitMappingEmitsAllConfiguredFields() {
        String locText = new AIcgdAllMetadata().toString_RawValues(
                AIsTextFormatProfiles.JSON_STANDARD, AInTextOutputObjectReferenceMode.NO_REFERENCES);
        assertTrue(locText.contains("\"$schemaId\":\"test.contract\""));
        assertTrue(locText.contains("\"$schemaVersion\":7"));
        assertTrue(locText.contains("\"$schema\":\"https://example.invalid/schema_7.jsondef.schema.json\""));
    }
}
