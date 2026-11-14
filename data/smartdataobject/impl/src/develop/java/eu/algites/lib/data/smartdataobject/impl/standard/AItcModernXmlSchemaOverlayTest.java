package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema;
import eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Java counterpart of the Python modern-only XML metadata regression.
 * General field metadata lives only on the common inherited contract,
 * whereas the XML QName is carried by the format-specific override.
 * @author linhart1
 */
public final class AItcModernXmlSchemaOverlayTest {
    /** Schema-neutral field contract. */
    @AIaDataObject(description = "Example")
    public interface AIigCommon {
        /** @return the example code. */
        @AIaDataObjectField(fieldName = "Code")
        String getCode();
    }

    /** XML contract with only the new XML metadata. */
    @AIaDataObjectXmlSchema(namespace = "urn:modern", namespaceLocalName = "Record")
    public interface AIigXml extends AIigCommon, AIiXmlDataObject {
        /** {@inheritDoc} */
        @Override
        @AIaDataObjectFieldXmlSchema(namespaceLocalName = "code", valueKind = AInXmlValueKind.ATTRIBUTE)
        String getCode();
    }

    /** Format-specific field metadata must attach to the general inherited getter. */
    @Test
    public void modernOnlyXmlSchemaOverlaysCommonContractField() {
        var descriptor = AIcSmartDataMetadata.forReadContract(AIigXml.class);
        Assert.assertEquals(descriptor.xmlSchema().namespace(), "urn:modern");
        Assert.assertEquals(descriptor.xmlSchema().namespaceLocalName(), "Record");
        Assert.assertEquals(descriptor.fields().size(), 1);
        var code = descriptor.fields().get(0);
        Assert.assertEquals(code.name(), "Code");
        Assert.assertEquals(code.xmlLocalName(), "code");
        Assert.assertEquals(code.xmlNamespace(), "");
        Assert.assertTrue(code.isXmlAttribute());
        Assert.assertEquals(code.annotation().fieldName(), "Code");
        Assert.assertEquals(code.getter().getDeclaringClass(), AIigCommon.class);
    }
}
