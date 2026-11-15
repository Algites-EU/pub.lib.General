package eu.algites.lib.data.dataobject;

import eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema;
import eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectFieldXmlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import java.lang.reflect.Method;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verify that format-specific schema identities coexist on one neutral field
 * contract without technology-specific DataObject inheritance.
 *
 * @author linhart1
 */
public final class AItcDataObjectSchemaMetadataCompatibilityTest {
    /** Check independent JSON/YAML identities and their versions. */
    @Test
    public void independentJsonAndYamlIdentities() {
        var json = CombinedContract.class.getAnnotation(AIaDataObjectJsonSchema.class);
        var yaml = CombinedContract.class.getAnnotation(AIaDataObjectYamlSchema.class);
        Assert.assertEquals(json.schemaId(), "json-sample");
        Assert.assertEquals(json.schemaVersion(), 7);
        Assert.assertEquals(yaml.schemaId(), "yaml-sample");
        Assert.assertEquals(yaml.schemaVersion(), 11);
    }

    /** Check that XML declaration and field QNames are retained independently. */
    @Test
    public void xmlSchemaMetadataPreservesExactQName() throws NoSuchMethodException {
        AIaDataObjectXmlSchema schema = CombinedContract.class.getAnnotation(AIaDataObjectXmlSchema.class);
        Method getter = CombinedContract.class.getDeclaredMethod("getCode");
        AIaDataObjectFieldXmlSchema field = getter.getAnnotation(AIaDataObjectFieldXmlSchema.class);
        Assert.assertEquals(schema.definitionKind(), AInXmlObjectDefinitionKind.TYPE);
        Assert.assertEquals(schema.namespace(), "urn:example");
        Assert.assertEquals(schema.namespaceLocalName(), "Sample");
        Assert.assertEquals(field.valueKind(), AInXmlValueKind.ATTRIBUTE);
        Assert.assertEquals(field.namespace(), "urn:example");
        Assert.assertEquals(field.namespaceLocalName(), "Code");
    }

    /** Ensure schema metadata does not create extra domain DataObject declarations. */
    @Test
    public void neutralFieldsRemainOnCommonContract() throws NoSuchMethodException {
        Assert.assertNotNull(CommonContract.class.getAnnotation(AIaDataObject.class));
        Assert.assertNull(CombinedContract.class.getDeclaredAnnotation(AIaDataObject.class));
        Assert.assertNotNull(CommonContract.class.getDeclaredMethod("getCode")
                .getAnnotation(AIaDataObjectField.class));
        Assert.assertNull(CombinedContract.class.getDeclaredMethod("getCode")
                .getAnnotation(AIaDataObjectField.class));
    }

    @AIaDataObject(description = "Neutral example")
    private interface CommonContract {
        /** @return sample field value */
        @AIaDataObjectField(fieldName = "code")
        String getCode();
    }

    @AIaDataObjectJsonSchema(schemaId = "json-sample", schemaVersion = 7)
    @AIaDataObjectYamlSchema(schemaId = "yaml-sample", schemaVersion = 11)
    @AIaDataObjectXmlSchema(namespace = "urn:example", namespaceLocalName = "Sample")
    private interface CombinedContract extends CommonContract {
        /** @return same logical field with an XML wire projection */
        @Override
        @AIaDataObjectFieldXmlSchema(namespace = "urn:example", namespaceLocalName = "Code",
                valueKind = AInXmlValueKind.ATTRIBUTE)
        String getCode();
    }
}
