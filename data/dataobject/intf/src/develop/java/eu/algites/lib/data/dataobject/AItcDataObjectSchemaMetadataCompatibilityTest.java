package eu.algites.lib.data.dataobject;

import eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema;
import eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema;
import eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema;

import eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject;
import eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Java half of the schema-metadata compatibility regression suite. The same
 * scenarios are covered by test_data_object_schema_metadata_compatibility.py.
 * @author linhart1
 */
public final class AItcDataObjectSchemaMetadataCompatibilityTest {
    /** Common JSON Schema metadata must not encode JSON vs YAML wire format. */
    @Test
    public void commonJsonSchemaWorksWithBothFormatMarkers() {
        Assert.assertEquals(JsonContract.class.getAnnotation(AIaDataObjectJsonSchema.class).schemaId(), "sample");
        Assert.assertEquals(YamlContract.class.getAnnotation(AIaDataObjectJsonSchema.class).schemaId(), "sample");
        Assert.assertEquals(JsonContract.class.getAnnotation(AIaDataObjectJsonSchema.class).schemaVersion(), 7);
        Assert.assertEquals(YamlContract.class.getAnnotation(AIaDataObjectJsonSchema.class).schemaVersion(), 7);
        Assert.assertEquals(JsonContract.class.getAnnotation(AIaJsonDataObject.class).schemaId(), "sample");
        Assert.assertEquals(YamlContract.class.getAnnotation(AIaYamlDataObject.class).schemaId(), "sample");
    }

    /** Both old and new XML Schema declarations preserve definition QName. */
    @Test
    public void xmlSchemaMetadataPreservesExactQName() {
        AIaDataObjectXmlSchema modern = XmlContract.class.getAnnotation(AIaDataObjectXmlSchema.class);
        AIaXmlDataObject legacy = XmlContract.class.getAnnotation(AIaXmlDataObject.class);
        Assert.assertEquals(modern.definitionKind(), AInXmlObjectDefinitionKind.TYPE);
        Assert.assertEquals(modern.namespace(), legacy.namespace());
        Assert.assertEquals(modern.namespaceLocalName(), legacy.namespaceLocalName());
    }

    /** Both XML field metadata forms retain element/attribute and QName semantics. */
    @Test
    public void xmlFieldMetadataPreservesExactQName() throws NoSuchMethodException {
        Method getter = XmlContract.class.getDeclaredMethod("getCode");
        AIaDataObjectFieldXmlSchema modern = getter.getAnnotation(AIaDataObjectFieldXmlSchema.class);
        AIaXmlDataObjectField legacy = getter.getAnnotation(AIaXmlDataObjectField.class);
        Assert.assertEquals(modern.valueKind(), AInXmlValueKind.ATTRIBUTE);
        Assert.assertEquals(modern.namespace(), legacy.namespace());
        Assert.assertEquals(modern.namespaceLocalName(), legacy.namespaceLocalName());
    }

    /** General annotations are owned by the format-neutral interface only. */
    @Test
    public void generalMetadataIsDeclaredOnlyOnCommonContract() throws NoSuchMethodException {
        Assert.assertNotNull(CommonContract.class.getAnnotation(AIaDataObject.class));
        Assert.assertNull(XmlContract.class.getDeclaredAnnotation(AIaDataObject.class));
        Assert.assertNotNull(CommonContract.class.getDeclaredMethod("getCode")
                .getAnnotation(AIaDataObjectField.class));
        Assert.assertNull(XmlContract.class.getDeclaredMethod("getCode")
                .getAnnotation(AIaDataObjectField.class));
    }

    /** Legacy annotation types are retained for a staged migration. */
    @Test
    public void legacyAnnotationsAreDeprecatedButAvailable() {
        for (Class<?> annotation : new Class<?>[]{AIaJsonDataObject.class,
                AIaYamlDataObject.class, AIaXmlDataObject.class, AIaXmlDataObjectField.class}) {
            Assert.assertNotNull(annotation.getAnnotation(Deprecated.class), annotation.getName());
        }
    }

    @AIaDataObject(description = "Neutral example")
    private interface CommonContract {
        @AIaDataObjectField(fieldName = "code")
        String getCode();
    }

    @AIaJsonDataObject(schemaId = "sample", schemaVersion = 7)
    @AIaDataObjectJsonSchema(schemaId = "sample", schemaVersion = 7)
    private interface JsonContract extends CommonContract,
            eu.algites.lib.data.dataobject.jsondefs.AIiJsonDataObject { }

    @AIaYamlDataObject(schemaId = "sample", schemaVersion = 7)
    @AIaDataObjectJsonSchema(schemaId = "sample", schemaVersion = 7)
    private interface YamlContract extends CommonContract,
            eu.algites.lib.data.dataobject.yamldefs.AIiYamlDataObject { }

    @AIaXmlDataObject(namespace = "urn:example", namespaceLocalName = "Sample")
    @AIaDataObjectXmlSchema(namespace = "urn:example", namespaceLocalName = "Sample")
    private interface XmlContract extends CommonContract,
            eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject {
        @Override
        @AIaXmlDataObjectField(namespace = "urn:example", namespaceLocalName = "Code",
                valueKind = AInXmlValueKind.ATTRIBUTE)
        @AIaDataObjectFieldXmlSchema(namespace = "urn:example", namespaceLocalName = "Code",
                valueKind = AInXmlValueKind.ATTRIBUTE)
        String getCode();
    }
}
