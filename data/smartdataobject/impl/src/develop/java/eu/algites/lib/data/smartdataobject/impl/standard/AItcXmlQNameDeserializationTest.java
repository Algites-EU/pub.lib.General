package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * End-to-end coverage of typed, QName-based XML input and rejected ambiguity.
 *
 * <p>These fixtures follow the generated interface/implementation contract:
 * read-only schema metadata lives on interfaces, mutable storage on the
 * concrete technology-specific implementation class.
 */
public final class AItcXmlQNameDeserializationTest {
    /** Nested XSD type whose QName is different from its parent element QName. */
    @AIaXmlDataObject(namespace = "urn:internal", namespaceLocalName = "AddressType")
    public interface AIiAddress extends AIiXmlDataObject {
        /** City name serialized under the global orders namespace. */
        @AIaDataObjectField(fieldName = "City")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "City")
        String getCity();
    }

    /** Concrete nested type, supplied explicitly to the parser. */
    public static final class AIcAddress extends AIcXmlSmartDataObject implements AIiAddress {
        /** Initializes the instance with no present fields. */
        public AIcAddress() { super(); }
        /** {@inheritDoc} */
        @Override public String getCity() { return (String) get_EffectiveField("City"); }
    }

    /** Read-only XML Schema root contract with attributes and repeated elements. */
    @AIaXmlDataObject(namespace = "urn:orders", namespaceLocalName = "Order")
    public interface AIiOrder extends AIiXmlDataObject {
        /** Required QName-qualified child field (read-only representation). */
        @AIaDataObjectField(fieldName = "Code")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "Code")
        String getCode();

        /** Unqualified attribute not confused with the child element namespace. */
        @AIaDataObjectField(fieldName = "Id")
        @AIaXmlDataObjectField(namespaceLocalName = "id", valueKind = AInXmlValueKind.ATTRIBUTE)
        String getId();

        /** Repeated sibling elements represented by a typed Java list. */
        @AIaDataObjectField(fieldName = "Entry")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "Entry")
        List<String> getEntry();

        /** Nested read-only contract backed by AIcAddress. */
        @AIaDataObjectField(fieldName = "Address")
        @AIaXmlDataObjectField(namespace = "urn:orders", namespaceLocalName = "Address")
        AIiAddress getAddress();
    }

    /** Mutable generated-style XML document. */
    public static final class AIcOrder extends AIcXmlSmartDataObject implements AIiOrder {
        /** Initializes the document with no explicitly present values. */
        public AIcOrder() { super(); }
        /** {@inheritDoc} */
        @Override public String getCode() { return (String) get_EffectiveField("Code"); }
        /** {@inheritDoc} */
        @Override public String getId() { return (String) get_EffectiveField("Id"); }
        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public List<String> getEntry() { return (List<String>) get_EffectiveField("Entry"); }
        /** {@inheritDoc} */
        @Override public AIiAddress getAddress() { return (AIiAddress) get_EffectiveField("Address"); }
    }

    /** Deserialize XML with arbitrary child ordering and preserve typed nested values. */
    @Test
    public void AIcQNameOrderIndependentTypedNestedFields() {
        String locXml = "<o:Order xmlns:o='urn:orders' id='A'>"
                + "<o:Address><o:City>Prague</o:City></o:Address>"
                + "<o:Entry>one</o:Entry><o:Code>X</o:Code><o:Entry>two</o:Entry></o:Order>";
        AIcOrder locOrder = AIcXmlSmartDataObject.fromXml(locXml, AIcOrder.class,
                Map.of("Address", AIcAddress.class));
        Assert.assertEquals(locOrder.getCode(), "X");
        Assert.assertEquals(locOrder.getId(), "A");
        Assert.assertEquals(locOrder.getEntry(), List.of("one", "two"));
        Assert.assertEquals(locOrder.getAddress().getCity(), "Prague");
        Assert.assertTrue(locOrder.getAddress() instanceof AIcAddress);
    }

    /** Canonical writer and QName parser preserve the same nested typed values. */
    @Test
    public void AIcXmlWriterReaderRoundTrip() {
        String locXml = "<Order xmlns='urn:orders' id='A'><Code>C</Code>"
                + "<Entry>x</Entry><Address><City>Pilsen</City></Address></Order>";
        AIcOrder locOriginal = AIcXmlSmartDataObject.fromXml(locXml, AIcOrder.class,
                Map.of("Address", AIcAddress.class));
        String locCanonical = locOriginal.toString_RawValues(
                eu.algites.lib.text.format.AIsTextFormatProfiles.XML_XSD,
                eu.algites.lib.text.format.AInTextOutputObjectReferenceMode.NO_REFERENCES);
        AIcOrder locParsed = AIcXmlSmartDataObject.fromXml(locCanonical, AIcOrder.class,
                Map.of("Address", AIcAddress.class));
        Assert.assertEquals(locParsed.getCode(), "C");
        Assert.assertEquals(locParsed.getEntry(), List.of("x"));
        Assert.assertEquals(locParsed.getAddress().getCity(), "Pilsen");
    }

    /** Missing repeated siblings map to an explicit empty collection. */
    @Test
    public void AIcMissingXmlListBecomesEmpty() {
        AIcOrder locOrder = AIcXmlSmartDataObject.fromXml("<Order xmlns='urn:orders' id='A'/>",
                AIcOrder.class, Map.of("Address", AIcAddress.class));
        Assert.assertEquals(locOrder.getEntry(), List.of());
    }

    /** Unknown QName content must be rejected instead of silently dropped. */
    @Test
    public void AIcUnknownXmlQNameIsRejected() {
        Assert.expectThrows(IllegalArgumentException.class, () ->
            AIcXmlSmartDataObject.fromXml("<Order xmlns='urn:orders'><Unknown/></Order>",
                    AIcOrder.class, Map.of()));
    }

    /** A non-repeating field must reject duplicate XML elements. */
    @Test
    public void AIcDuplicateScalarIsRejected() {
        Assert.expectThrows(IllegalArgumentException.class, () ->
            AIcXmlSmartDataObject.fromXml("<Order xmlns='urn:orders'><Code>A</Code><Code>B</Code></Order>",
                    AIcOrder.class, Map.of()));
    }

    /** XML DTD declarations must never reach an entity resolver. */
    @Test
    public void AIcExternalEntityIsRejected() {
        Assert.expectThrows(IllegalArgumentException.class, () ->
            AIcXmlSmartDataObject.fromXml("<!DOCTYPE Order [<!ENTITY x 'value'>]>"
                    + "<Order xmlns='urn:orders'/>", AIcOrder.class, Map.of()));
    }

    /** XML contract with explicit wire minima and maxima for scalar and collection elements. */
    @AIaXmlDataObject(namespace = "urn:bounded", namespaceLocalName = "Root")
    public interface AIiBoundedXml extends AIiXmlDataObject {
        /** One mandatory scalar occurrence. */
        @AIaDataObjectField(fieldName = "Required")
        @AIaXmlDataObjectField(namespace = "urn:bounded", namespaceLocalName = "Required",
                minOccurs = 1, maxOccurs = 1)
        String getRequired();

        /** One or two XML siblings with identical expanded QName. */
        @AIaDataObjectField(fieldName = "Entry")
        @AIaXmlDataObjectField(namespace = "urn:bounded", namespaceLocalName = "Entry",
                minOccurs = 1, maxOccurs = 2)
        List<String> getEntry();
    }

    /** Concrete mutable holder for bounded XML declarations. */
    public static final class AIcBoundedXml extends AIcXmlSmartDataObject implements AIiBoundedXml {
        /** Initializes the holder before XML fields are populated. */
        public AIcBoundedXml() { super(); }
        /** {@inheritDoc} */
        @Override public String getRequired() { return (String) get_EffectiveField("Required"); }
        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public List<String> getEntry() { return (List<String>) get_EffectiveField("Entry"); }
    }

    /** Both scalar and repeated XML fields must satisfy generated cardinality declarations. */
    @Test
    public void AIcXmlOccurrenceBoundsRejectMissingAndExcessValues() {
        String locValid = "<Root xmlns='urn:bounded'><Entry>A</Entry>"
                + "<Required>X</Required><Entry>B</Entry></Root>";
        AIcBoundedXml locValue = AIcXmlSmartDataObject.fromXml(locValid, AIcBoundedXml.class, Map.of());
        Assert.assertEquals(locValue.getRequired(), "X");
        Assert.assertEquals(locValue.getEntry(), List.of("A", "B"));
        Assert.expectThrows(IllegalArgumentException.class, () -> AIcXmlSmartDataObject.fromXml(
                "<Root xmlns='urn:bounded'><Entry>A</Entry></Root>", AIcBoundedXml.class, Map.of()));
        Assert.expectThrows(IllegalArgumentException.class, () -> AIcXmlSmartDataObject.fromXml(
                "<Root xmlns='urn:bounded'><Required>X</Required></Root>", AIcBoundedXml.class, Map.of()));
        Assert.expectThrows(IllegalArgumentException.class, () -> AIcXmlSmartDataObject.fromXml(
                "<Root xmlns='urn:bounded'><Required>X</Required><Entry>A</Entry>"
                + "<Entry>B</Entry><Entry>C</Entry></Root>", AIcBoundedXml.class, Map.of()));
    }

}
