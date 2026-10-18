package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIaSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIaSmartDataObjectField;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue;
import eu.algites.lib.data.smartdataobject.AIxSmartDataValidationException;
import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaFieldDefault;
import eu.algites.lib.factory.cpdiscovery.AIaCategoryProviderFactoryDefinition;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolverFactory;
import eu.algites.lib.data.dataobject.xmldefs.*;
import eu.algites.lib.factory.cpdiscovery.AIsCategoryProviderDiscovery;
import eu.algites.lib.factory.cpdiscovery.impl.standard.AIcCategoryProviderRegistry;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;
import eu.algites.lib.data.dataobject.AIiDataObject;

import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import java.util.Set;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;

/** TestNG coverage for SmartDataObject runtime and serialization. */
public final class AItcSmartDataObjectTest {
    /** Registers the development-only provider before runtime default resolution. */
    @org.testng.annotations.BeforeClass
    public void configureFactoryDiscovery() {
        AIcCategoryProviderRegistry locRegistry = new AIcCategoryProviderRegistry();
        locRegistry.registerClassLoaderFactories(getClass().getClassLoader());
        AIsCategoryProviderDiscovery.setRegistry(locRegistry);
    }

    @AIaDataObject(schemaId = "test-options", schemaVersion = 1, fingerprint = "xyz456")
    public interface AIigOptions extends AIiDataObject {
        @AIaDataObjectField(fieldName = "Host", presence = AInFieldPresenceKind.REQUIRED)
        String getHost();
        @AIaDataObjectField(fieldName = "Secure", presence = AInFieldPresenceKind.REQUIRED)
        Boolean getSecure();
    }
    @AIaDataObject(schemaId = "test-settings", schemaVersion = 1, fingerprint = "abc123")
    @AIaXmlDataObject(namespaceLocalName = "Settings", namespace = "urn:settings", definitionKind = AInXmlObjectDefinitionKind.ELEMENT)
    public interface AIigDemo extends AIiXmlDataObject {
        @AIaXmlDataObjectField(namespace = "urn:field")
        @AIaDataObjectField(fieldName = "Name", presence = AInFieldPresenceKind.REQUIRED)
        String getName();
        @AIaDataObjectField(fieldName = "Port", presence = AInFieldPresenceKind.REQUIRED, allowsNull = false)
        @AIaFieldDefault(defaultValueResolver = @AIaCategoryProviderFactoryDefinition(
                factoryClass = AIiFieldDefaultValueResolverFactory.class, factoryProviderId = "test-values"))
        Integer getPort();
        @AIaDataObjectField(fieldName = "Child", allowsNull = true)
        AIigDemo getChild();
        @AIaDataObjectField(fieldName = "Options")
        @AIaFieldDefault(defaultValueResolver = @AIaCategoryProviderFactoryDefinition(
                factoryClass = AIiFieldDefaultValueResolverFactory.class, factoryProviderId = "test-values"))
        AIigOptions getOptions();
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
        public AIcgdDemo() { super(); }
        public AIcgdDemo(Map<String, AIidVirtualFieldValue> aMapping) { super(aMapping); }
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

    @AIaDataObject(schemaId = "test-read-policy", schemaVersion = 1)
    @AIaSmartDataObject(allowNonPresentRead = true)
    public interface AIigReadPolicy extends AIiDataObject {
        @AIaDataObjectField(presence = AInFieldPresenceKind.REQUIRED)
        String getOptionalRead();
        @AIaDataObjectField(fieldName = "StrictRead", presence = AInFieldPresenceKind.REQUIRED)
        @AIaSmartDataObjectField(allowNonPresentRead = false)
        String getStrictRead();
        @AIaDataObjectField(fieldName = "DefaultRead", presence = AInFieldPresenceKind.REQUIRED)
        @AIaFieldDefault(defaultValueResolver = @AIaCategoryProviderFactoryDefinition(
                factoryClass = AIiFieldDefaultValueResolverFactory.class, factoryProviderId = "test-values"))
        String getDefaultRead();
    }
    public static final class AIcgdReadPolicy extends AIcSmartDataObject implements AIigReadPolicy {
        public String getOptionalRead() { return (String) get_EffectiveField("OptionalRead"); }
        public String getStrictRead() { return (String) get_EffectiveField("StrictRead"); }
        public String getDefaultRead() { return (String) get_EffectiveField("DefaultRead"); }
        public String getRawOptionalRead() { return (String) get_RawField("OptionalRead"); }
        public boolean isOptionalReadPresent() { return isPresent_Field("OptionalRead"); }
    }

    private static void verify(boolean aCondition, String aMessage) {
        if (!aCondition) throw new AssertionError(aMessage);
    }
    @Test
    public void verifySmartDataObjectBehavior() {
        AIcgdReadPolicy locPolicy = new AIcgdReadPolicy();
        verify(locPolicy.getOptionalRead() == null, "SmartDO absent-read policy");
        verify(locPolicy.getRawOptionalRead() == null && !locPolicy.isOptionalReadPresent(),
                "raw/presence unchanged");
        verify(locPolicy.getDefaultRead().equals("default"), "default precedes absent-read policy");
        verify(locPolicy.get_RawNotPresentRequiredFields().contains("OptionalRead"),
                "required validation unchanged");
        boolean locStrictFailed = false;
        try { locPolicy.getStrictRead(); }
        catch (IllegalStateException aException) { locStrictFailed = true; }
        verify(locStrictFailed, "getter-level false overrides interface-level true");
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
    }

    @Test
    public void verifyTypedMappingPreservesPolymorphismAndCycles() {
        AIcgdDemo locA = new AIcgdDemo();
        AIcgdDemo locB = new AIcgdDemo();
        locA.setName("A");
        locB.setName("B");
        locA.setChild(locB);
        locB.setChild(locA);
        Map<String, AIidVirtualFieldValue> locVirtual = locA.toMapping_RawValues();
        verify(locVirtual.get("Child").getValueClass() == AIcgdDemo.class,
                "typed node must preserve the concrete implementation class");
        var locMappedB = locVirtual.get("Child").getValueRepresentation();
        verify(((Map<?, ?>) locMappedB).get("Child") instanceof AIidVirtualFieldValue,
                "all field values must use the uniform wrapper");
        verify(((AIidVirtualFieldValue) ((Map<?, ?>) locMappedB).get("Child"))
                .getValueRepresentation() == locVirtual, "cycle is represented by identity");
        AIcgdDemo locRestored = AIsSmartDataObjectFactory.fromMapping(AIcgdDemo.class, locVirtual);
        verify(locRestored.get_Child().get_Child() == locRestored, "factory preserves cycle");
        verify(locRestored.get_Child().getName().equals("B"), "typed nested SDO restored");
        AIcgdDemo locDirect = new AIcgdDemo(locVirtual);
        verify(locDirect.get_Child().get_Child() == locDirect, "public mapping constructor preserves cycle");
    }

}
