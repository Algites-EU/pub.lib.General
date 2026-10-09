package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;

import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import java.util.Set;

/** Standalone assertion smoke test; can run without external test libraries. */
public final class AItcSmartDataObjectSmokeTest {
    @AIaDataObject(id = "test-options", version = 1, fingerprint = "xyz456")
    public interface AIigOptions extends AIiDataObject {
        @AIaDataObjectField(name = "Host", presenceRequired = true)
        String getHost();
        @AIaDataObjectField(name = "Secure", presenceRequired = true)
        Boolean getSecure();
    }
    @AIaDataObject(id = "test-settings", version = 1, fingerprint = "abc123",
            xmlLocalName = "Settings", xmlNamespace = "urn:settings")
    public interface AIigDemo extends AIiDataObject {
        @AIaDataObjectField(name = "Name", presenceRequired = true, xmlNamespace = "urn:field")
        String getName();
        @AIaDataObjectField(name = "Port", presenceRequired = true, defaultFactoryMethod = "defaultPort")
        Integer getPort();
        @AIaDataObjectField(name = "Child", allowsNull = true)
        AIigDemo getChild();
        @AIaDataObjectField(name = "Options", defaultFactoryMethod = "defaultOptions")
        AIigOptions getOptions();
        static Integer defaultPort() { return 8080; }
        static AIigOptions defaultOptions() {
            return new AIigOptions() {
                @Override public String getHost() { return "localhost"; }
                @Override public Boolean getSecure() { return Boolean.TRUE; }
            };
        }
    }
    public interface AIigdDemo extends AIigDemo, AIiSmartDataObject {
        void setName(String aName);
        String get_Name();
        boolean isPresent_Name();
        void unset_Name();
        void setPort(Integer aPort);
        Integer get_Port();
        boolean isPresent_Port();
        void unset_Port();
        void setOptions(AIigOptions aOptions);
        AIigOptions get_Options();
        boolean isPresent_Options();
        void unset_Options();
        void setChild(AIigdDemo aChild);
        AIigdDemo get_Child();
        boolean isPresent_Child();
        void unset_Child();
    }
    public static final class AIcgdDemo extends AIcSmartDataObject implements AIigdDemo {
        @Override public String getName() { return (String) get_EffectiveField("Name"); }
        @Override public void setName(String aName) { set_RawField("Name", aName); }
        @Override public String get_Name() { return (String) get_RawField("Name"); }
        @Override public boolean isPresent_Name() { return isPresent_Field("Name"); }
        @Override public void unset_Name() { unset_Field("Name"); }
        @Override public Integer getPort() { return (Integer) get_EffectiveField("Port"); }
        @Override public void setPort(Integer aPort) { set_RawField("Port", aPort); }
        @Override public Integer get_Port() { return (Integer) get_RawField("Port"); }
        @Override public boolean isPresent_Port() { return isPresent_Field("Port"); }
        @Override public void unset_Port() { unset_Field("Port"); }
        @Override public AIigOptions getOptions() { return (AIigOptions) get_EffectiveField("Options"); }
        @Override public void setOptions(AIigOptions aOptions) { set_RawField("Options", aOptions); }
        @Override public AIigOptions get_Options() { return (AIigOptions) get_RawField("Options"); }
        @Override public boolean isPresent_Options() { return isPresent_Field("Options"); }
        @Override public void unset_Options() { unset_Field("Options"); }
        @Override public AIigDemo getChild() { return (AIigDemo) get_EffectiveField("Child"); }
        @Override public void setChild(AIigdDemo aChild) { set_RawField("Child", aChild); }
        @Override public AIigdDemo get_Child() { return (AIigdDemo) get_RawField("Child"); }
        @Override public boolean isPresent_Child() { return isPresent_Field("Child"); }
        @Override public void unset_Child() { unset_Field("Child"); }
    }

    private static void verify(boolean aCondition, String aMessage) {
        if (!aCondition) throw new AssertionError(aMessage);
    }
    public static void main(String[] aArgs) {
        if (aArgs.length > 0 && "--yaml".equals(aArgs[0])) {
            AIcgdDemo locExample = new AIcgdDemo();
            locExample.setName("Example");
            locExample.setChild(locExample);
            System.out.print(locExample.toString_EffectiveValues(AIsTextFormatProfiles.YAML_NATIVE_REFERENCES,
                    AInTextOutputObjectReferenceMode.REPEATED_OBJECTS));
            return;
        }
        AIcgdDemo locA = new AIcgdDemo();
        locA.setName("A");
        verify(locA.getPort() == 8080, "effective default");
        verify(locA.get_Port() == null && !locA.isPresent_Port(), "raw default must remain absent");
        verify(locA.get_RawNotPresentRequiredFields().equals(Set.of("Port")), "raw presence");
        verify(locA.get_EffectiveNotPresentRequiredFields().isEmpty(), "effective presence");
        verify(!locA.is_ByRawValuesValid() && locA.is_ByEffectiveValuesValid(), "local validity");
        String locRaw = locA.toString_RawValues(AIsTextFormatProfiles.JSON_STANDARD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        String locEffective = locA.toString_EffectiveValues(AIsTextFormatProfiles.JSON_STANDARD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        verify(locRaw.contains("\"Name\":\"A\"") && !locRaw.contains("Port"), "raw JSON");
        verify(locEffective.contains("\"Port\":8080"), "effective JSON");
        verify(locEffective.contains("\"Options\":{\"Host\":\"localhost\",\"Secure\":true}"),
                "read-only structured default must serialize using annotated getters");
        String locXml = locA.toString_EffectiveValues(AIsTextFormatProfiles.XML_XSD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES);
        verify(locXml.contains("xmlns:ns1=\"urn:settings\"") &&
                locXml.contains("xmlns:ns2=\"urn:field\"") &&
                locXml.contains("<ns2:Name>A</ns2:Name>"), "XSD namespace-qualified XML");
        AIcgdDemo locB = new AIcgdDemo();
        locA.setChild(locB);
        locB.setChild(locA);
        verify(locA.is_ByEffectiveValuesValid(), "local validity ignores nested missing values");
        verify(!locA.is_DeeplyByEffectiveValuesValid(), "deep validation catches child");
        verify(locA.get_DeeplyEffectiveNotPresentRequiredFields().contains("Child.Name"), "deep path");
        boolean locEffectiveFailed = false;
        try { locA.toString_EffectiveValues(AIsTextFormatProfiles.JSON_DOTNET_PRESERVE,
                AInTextOutputObjectReferenceMode.REPEATED_OBJECTS); }
        catch (AIxSmartDataValidationException aExpected) { locEffectiveFailed = true; }
        verify(locEffectiveFailed, "invalid deep values must not be silently serialized as effective wire data");
        locB.setName("B");
        String locGraph = locA.toString_EffectiveValues(AIsTextFormatProfiles.JSON_DOTNET_PRESERVE,
                AInTextOutputObjectReferenceMode.REPEATED_OBJECTS);
        verify(locGraph.contains("\"$id\"") && locGraph.contains("\"$ref\""), "JSON identity preservation");
        String locDiagnostic = locA.toString();
        verify(locDiagnostic.contains("dNr=") && locDiagnostic.contains("dNrRef="), "diagnostic cycles");
        boolean locFailed = false;
        try { locA.toString_RawValues(AIsTextFormatProfiles.JSON_STANDARD,
                AInTextOutputObjectReferenceMode.NO_REFERENCES); }
        catch (IllegalStateException aExpected) { locFailed = true; }
        verify(locFailed, "standard JSON must reject cycles");
        locA.unset_Child();
        verify(!locA.isPresent_Child() && locA.get_Child() == null, "unset operation");
        locA.setPort(null);
        verify(locA.isPresent_Port() && !locA.is_ByEffectiveValuesValid(), "present null violates nullability");
        System.out.println("JAVA_SMARTDO_SMOKE_PASS");
    }
    private AItcSmartDataObjectSmokeTest() { }
}
