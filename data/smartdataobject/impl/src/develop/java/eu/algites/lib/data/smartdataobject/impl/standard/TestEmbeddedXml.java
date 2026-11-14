package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import java.util.List;
import java.util.Map;

/** Integration regression for transparent XML groups. @author linhart1 */
public final class TestEmbeddedXml {
    private static final String XML = "<Employee xmlns=\"urn:person\">"
            + "<FirstName>John</FirstName><LastName>Smith</LastName>"
            + "<FirstName>Jane</FirstName><LastName>Doe</LastName>"
            + "<Department>IT</Department></Employee>";

    /** One reusable nominal group with no wire wrapper. @author linhart1 */
    public static class PersonIdentity extends AIcXmlSmartDataObject {
        /** Return first name. @return first name */
        @AIaDataObjectField(fieldName = "FirstName", order = 0, minItemCount = 1)
        @AIaXmlDataObjectField(namespace = "urn:person", namespaceLocalName = "FirstName")
        public String getFirstName() { return (String) get_RawField("FirstName"); }
        /** Set first name. @param value first name */
        public void setFirstName(String value) { set_RawField("FirstName", value); }
        /** Return last name. @return last name */
        @AIaDataObjectField(fieldName = "LastName", order = 1, minItemCount = 1)
        @AIaXmlDataObjectField(namespace = "urn:person", namespaceLocalName = "LastName")
        public String getLastName() { return (String) get_RawField("LastName"); }
        /** Set last name. @param value last name */
        public void setLastName(String value) { set_RawField("LastName", value); }
    }

    /** Two occurrences of the same nominal group. @author linhart1 */
    @AIaXmlDataObject(namespace = "urn:person", namespaceLocalName = "Employee")
    public static final class Employee extends AIcXmlSmartDataObject {
        /** Return first embedded group. @return first group */
        @AIaDataObjectField(embedded = true, order = 0, minItemCount = 1, maxItemCount = 1)
        public PersonIdentity getIdentityGroup1() { return (PersonIdentity) get_RawField("IdentityGroup1"); }
        /** Return second embedded group. @return second group */
        @AIaDataObjectField(embedded = true, order = 1, minItemCount = 1, maxItemCount = 1)
        public PersonIdentity getIdentityGroup2() { return (PersonIdentity) get_RawField("IdentityGroup2"); }
        /** Return department. @return department */
        @AIaDataObjectField(fieldName = "Department", order = 2)
        @AIaXmlDataObjectField(namespace = "urn:person", namespaceLocalName = "Department")
        public String getDepartment() { return (String) get_RawField("Department"); }
    }

    /** Repeated group occurrence, not two separately named fields. @author linhart1 */
    @AIaXmlDataObject(namespace = "urn:person", namespaceLocalName = "Employee")
    public static final class RepeatedEmployee extends AIcXmlSmartDataObject {
        /** Return list of transparent group occurrences. @return groups */
        @AIaDataObjectField(embedded = true, order = 0, minItemCount = 2, maxItemCount = 2)
        public List<PersonIdentity> getIdentities() {
            @SuppressWarnings("unchecked") List<PersonIdentity> list = (List<PersonIdentity>) get_RawField("Identities");
            return list;
        }
        /** Return department. @return department */
        @AIaDataObjectField(fieldName = "Department", order = 1)
        @AIaXmlDataObjectField(namespace = "urn:person", namespaceLocalName = "Department")
        public String getDepartment() { return (String) get_RawField("Department"); }
    }

    /** Sealed immutable alternatives remain nominally distinct. @author linhart1 */
    public sealed interface PersonChoice permits ValueChoice, IdentityChoice { }

    /** One scalar alternative; no XML wrapper named Choice. @param field1 actual value */
    public record ValueChoice(String field1) implements PersonChoice {
        /** @return the selected literal */
        @AIaDataObjectField(fieldName = "Value", order = 0)
        @AIaXmlDataObjectField(namespace = "urn:person", namespaceLocalName = "Value")
        public String getValue() { return field1; }
    }

    /** One embedded sequence alternative. @param field1 reusable identity group */
    public record IdentityChoice(PersonIdentity field1) implements PersonChoice {
        /** @return the selected embedded identity */
        @AIaDataObjectField(embedded = true, order = 0)
        public PersonIdentity getNameRegexp() { return field1; }
    }

    /** Object containing a closed choice, not a flattened bag of optional fields. */
    @AIaXmlDataObject(namespace = "urn:person", namespaceLocalName = "ChoiceSample")
    public static final class ChoiceSample extends AIcXmlSmartDataObject {
        /** @return selected immutable alternative */
        @AIaDataObjectField(embedded = true, order = 0, minItemCount = 1, maxItemCount = 1)
        public PersonChoice getChoice() { return (PersonChoice) get_RawField("Choice"); }
        /** @param value selected immutable alternative */
        public void setChoice(PersonChoice value) { set_RawField("Choice", value); }
    }

    /** Execute both occurrence styles and sealed variant roundtrips. @param args unused arguments */
    public static void main(String[] args) {
        Employee parsed = AIcXmlSmartDataObject.fromXml(XML, Employee.class, Map.of());
        if (!parsed.getIdentityGroup1().getFirstName().equals("John")
                || !parsed.getIdentityGroup2().getFirstName().equals("Jane"))
            throw new AssertionError("Embedded groups did not retain independent identity");
        String output = parsed.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        if (output.contains("IdentityGroup") || !output.contains("<")
                || !AIcXmlSmartDataObject.fromXml(output, Employee.class, Map.of())
                        .getIdentityGroup2().getLastName().equals("Doe"))
            throw new AssertionError("Embedded group XML roundtrip failed: " + output);
        RepeatedEmployee repeated = AIcXmlSmartDataObject.fromXml(XML, RepeatedEmployee.class, Map.of());
        if (repeated.getIdentities().size() != 2) throw new AssertionError("Wrong group count");
        String repeatedXml = repeated.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        if (AIcXmlSmartDataObject.fromXml(repeatedXml, RepeatedEmployee.class, Map.of())
                .getIdentities().size() != 2)
            throw new AssertionError("Repeated group XML roundtrip failed");
        ChoiceSample selected = new ChoiceSample();
        selected.setChoice(new ValueChoice("hello"));
        String selectedXml = selected.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        if (!selectedXml.contains("Value>hello</") || selectedXml.contains("<Choice>"))
            throw new AssertionError("Sealed scalar choice was not flattened: " + selectedXml);
        var selectedCopy = AIcXmlSmartDataObject.fromXml(selectedXml, ChoiceSample.class, Map.of());
        if (!(selectedCopy.getChoice() instanceof ValueChoice value)
                || !value.getValue().equals("hello"))
            throw new AssertionError("Scalar choice variant was lost");
        PersonIdentity identity = new PersonIdentity();
        identity.setFirstName("Alice"); identity.setLastName("Smith");
        selected.setChoice(new IdentityChoice(identity));
        selectedXml = selected.toString_RawValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        if (!selectedXml.contains("FirstName>Alice</") || selectedXml.contains("<NameRegexp>"))
            throw new AssertionError("Paired choice was not embedded: " + selectedXml);
        selectedCopy = AIcXmlSmartDataObject.fromXml(selectedXml, ChoiceSample.class,
                Map.of("Choice.NameRegexp", PersonIdentity.class));
        if (!(selectedCopy.getChoice() instanceof IdentityChoice pair)
                || !pair.getNameRegexp().getFirstName().equals("Alice"))
            throw new AssertionError("Paired choice variant was lost");
        System.out.println("Embedded XML groups and sealed choices roundtrip: OK");
    }
}
