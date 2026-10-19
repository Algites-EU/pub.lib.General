package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/** Verifies that XSD array values are rendered as repeated QName siblings. */
public final class AItcXmlRepeatedElementSerializationTest {
    /** XML declaration containing one regular field and a repeating element. */
    @AIaXmlDataObject(namespace = "urn:orders", namespaceLocalName = "Order")
    public interface AIiOrder extends AIiXmlDataObject {
        /** Single scalar field preceding the repeating fields. */
        @AIaDataObjectField(fieldName = "Code")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "Code", order = 0)
        String getCode();

        /** Repeating element whose QName is shared by all items. */
        @AIaDataObjectField(fieldName = "Entry")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "Entry", order = 1)
        List<String> getEntry();
    }

    /** Mutable implementation of the test schema contract. */
    public static final class AIcOrder extends AIcSmartDataObject implements AIiOrder {
        /** Construct an empty XML document. */
        public AIcOrder() { super(); }
        /** {@inheritDoc} */
        @Override public String getCode() { return (String) get_EffectiveField("Code"); }
        /** {@inheritDoc} */
        @Override @SuppressWarnings("unchecked") public List<String> getEntry() {
            return (List<String>) get_EffectiveField("Entry");
        }
        /** Set the scalar code field. */
        public void setCode(String aCode) { set_RawField("Code", aCode); }
        /** Set the repeating XML element list. */
        public void setEntry(List<String> aValues) { set_RawField("Entry", aValues); }
    }

    /** Generated enums expose their canonical serialized value through wireValue. */
    public enum AInExampleStatus {
        /** An intentionally different wire spelling from the Java identifier. */
        APPROVED("approved-wire");
        private final String wire;
        AInExampleStatus(String aWire) { wire = aWire; }
        /** Return the value preserved from the schema enumeration. */
        public String wireValue() { return wire; }
    }

    /** Read-only XML contract with a generated-style enumerated field. */
    public interface AIiStatusDocument extends AIiXmlDataObject {
        /** Return the schema-specific wire status. */
        @AIaDataObjectField(fieldName = "Status")
        @AIaXmlDataObjectField(namespaceLocalName = "Status")
        AInExampleStatus getStatus();
    }

    /** Mutable fixture using the normal SmartDataObject graph renderer. */
    public static final class AIcStatusDocument extends AIcSmartDataObject implements AIiStatusDocument {
        /** Creates a document with no present status. */
        public AIcStatusDocument() { super(); }
        /** {@inheritDoc} */
        @Override public AInExampleStatus getStatus() {
            return (AInExampleStatus) get_EffectiveField("Status");
        }
        /** Set the declared enum value. */
        public void setStatus(AInExampleStatus aStatus) { set_RawField("Status", aStatus); }
    }

    /** Verify that XSD wire strings, not Java enum identifiers, are emitted. */
    @Test
    public void AIcEnumWireValueIsPreservedInXml() {
        var locDocument = new AIcStatusDocument();
        locDocument.setStatus(AInExampleStatus.APPROVED);
        String locXml = locDocument.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        Assert.assertEquals(locXml.contains("<Status>approved-wire</Status>"), true);
        Assert.assertFalse(locXml.contains("APPROVED"));
    }

    /** Attribute-valued XML schema contract using the same generated enum. */
    public interface AIiStatusAttributeDocument extends AIiXmlDataObject {
        /** Return the XML attribute containing the wire enum token. */
        @AIaDataObjectField(fieldName = "Status")
        @AIaXmlDataObjectField(namespaceLocalName = "status",
                valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE)
        AInExampleStatus getStatus();
    }

    /** Mutable fixture with a scalar enum-valued XML attribute. */
    public static final class AIcStatusAttributeDocument extends AIcSmartDataObject
            implements AIiStatusAttributeDocument {
        /** Start without explicitly present attribute values. */
        public AIcStatusAttributeDocument() { super(); }
        /** {@inheritDoc} */
        @Override public AInExampleStatus getStatus() {
            return (AInExampleStatus) get_EffectiveField("Status");
        }
        /** Assign an XML status attribute. */
        public void setStatus(AInExampleStatus aStatus) { set_RawField("Status", aStatus); }
    }

    /** Attributes and child elements must share the same schema enum value. */
    @Test
    public void AIcXmlAttributeUsesEnumWireValue() {
        var locDocument = new AIcStatusAttributeDocument();
        locDocument.setStatus(AInExampleStatus.APPROVED);
        String locXml = locDocument.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        Assert.assertEquals(locXml.contains("status=\"approved-wire\""), true);
        Assert.assertFalse(locXml.contains("APPROVED"));
    }

    /** Confirm that empty lists disappear and nonempty lists create direct child siblings. */
    @Test
    public void AIcRepeatedElementsHaveMatchingQNames() throws Exception {
        AIcOrder locOrder = new AIcOrder();
        locOrder.setCode("A");
        locOrder.setEntry(List.of("first", "second"));
        String locXml = locOrder.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        DocumentBuilderFactory locFactory = DocumentBuilderFactory.newInstance();
        locFactory.setNamespaceAware(true);
        Element locRoot = locFactory.newDocumentBuilder()
                .parse(new org.xml.sax.InputSource(new java.io.StringReader(locXml)))
                .getDocumentElement();
        Assert.assertEquals(locRoot.getNamespaceURI(), "urn:orders");
        Assert.assertEquals(locRoot.getLocalName(), "Order");
        Assert.assertEquals(locRoot.getElementsByTagNameNS("urn:orders", "Code").getLength()
                + locRoot.getElementsByTagNameNS("urn:orders", "Entry").getLength(), 3);
        Assert.assertEquals(locRoot.getElementsByTagNameNS("urn:orders", "Code").getLength(), 1);
        Assert.assertEquals(locRoot.getElementsByTagNameNS("urn:orders", "Entry").getLength(), 2);
        Assert.assertEquals(locRoot.getElementsByTagName("Item").getLength(), 0);
        locOrder.setEntry(List.of());
        String locEmptyXml = locOrder.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        Assert.assertFalse(locEmptyXml.contains("Entry"));
    }
}
