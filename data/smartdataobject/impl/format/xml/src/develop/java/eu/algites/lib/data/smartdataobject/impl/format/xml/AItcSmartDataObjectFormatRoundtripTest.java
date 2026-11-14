package eu.algites.lib.data.smartdataobject.impl.format.xml;

import eu.algites.lib.data.dataobject.*;
import eu.algites.lib.data.dataobject.jsonschema.*;
import eu.algites.lib.data.dataobject.xmlschema.*;
import eu.algites.lib.data.dataobject.xmldefs.*;
import eu.algites.lib.data.smartdataobject.impl.common.*;
import eu.algites.lib.data.smartdataobject.impl.format.json.*;
import eu.algites.lib.data.smartdataobject.impl.format.yaml.*;
import eu.algites.lib.data.smartdataobject.impl.format.xml.*;

/**
 * Full independent-format roundtrip and ServiceLoader discovery regression.
 * Execute {@link #main(String[])} with format module service resources on classpath.
 *
 * @author linhart1
 */
public final class AItcSmartDataObjectFormatRoundtripTest {
    @AIaDataObjectJsonSchema(schemaId="person")
    @AIaDataObjectXmlSchema(namespace="urn:people", namespaceLocalName="Person")
    interface Person extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="name")
        @AIaDataObjectFieldXmlSchema(namespace="urn:people", namespaceLocalName="Name")
        String getName();
        @AIaDataObjectField(dataFieldName="age")
        @AIaDataObjectFieldXmlSchema(namespaceLocalName="age", valueKind=AInXmlValueKind.ATTRIBUTE)
        Integer getAge();
        @AIaDataObjectField(dataFieldName="address")
        @AIaDataObjectFieldXmlSchema(namespace="urn:people", namespaceLocalName="Address")
        Address getAddress();
    }
    @AIaDataObjectJsonSchema(schemaId="address")
    @AIaDataObjectXmlSchema(namespace="urn:addr", namespaceLocalName="Address")
    interface Address extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="city")
        @AIaDataObjectFieldXmlSchema(namespace="urn:addr", namespaceLocalName="City")
        String getCity();
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Address.class})
    public static class AddressSDO extends AIcSmartDataObject implements Address {
        public AddressSDO() { }
        @Override public String getCity() { return (String)get_EffectiveField("City"); }
        public void setCity(String city) { set_RawField("City", city); }
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Person.class})
    public static class PersonSDO extends AIcSmartDataObject implements Person {
        public PersonSDO() { }
        @Override public String getName() { return (String)get_EffectiveField("Name"); }
        @Override public Integer getAge() { return (Integer)get_EffectiveField("Age"); }
        @AIaSmartDataObjectFieldImplementationBinding(implementationClass=AddressSDO.class)
        @Override public Address getAddress() { return (Address)get_EffectiveField("Address"); }
        public void setName(String name) { set_RawField("Name", name); }
        public void setAge(Integer age) { set_RawField("Age", age); }
        public void setAddress(Address address) { set_RawField("Address", address); }
    }
    /**
     * Runs the three codec roundtrips and verifies Java service discovery.
     *
     * @param args unused command line arguments
     * @throws AssertionError if a format is missing or a roundtrip is invalid
     */
    public static void main(String[] args) {
        var registry = AIcSmartDataObjectBindingRegistry.builder()
                .register(PersonSDO.class).register(AddressSDO.class).build();
        AddressSDO address = new AddressSDO();
        address.setCity("Brno");
        PersonSDO person = new PersonSDO();
        person.setName("Příliš žluťoučký");
        person.setAge(42);
        person.setAddress(address);

        var providers = AIsSmartDataObjectFormatBindingProviderRegistry.getProviders();
        var formats = new java.util.LinkedHashSet<String>();
        for (var provider : providers) {
            formats.add(provider.getFormatId());
            String serialized = provider.createMarshaller().marshal(person, registry);
            Person restored = (Person) provider.createUnmarshaller()
                    .unmarshal(serialized, Person.class, registry);
            if (!"Příliš žluťoučký".equals(restored.getName()) ||
                    restored.getAge() != 42 ||
                    !"Brno".equals(restored.getAddress().getCity())) {
                throw new AssertionError("Format roundtrip failed: " + provider.getFormatId());
            }
            if (AIsSmartDataObjectFormatBindingProviderRegistry
                    .getProviders(provider.getFormatId()).stream()
                    .noneMatch(candidate -> candidate.getClass() == provider.getClass())) {
                throw new AssertionError("Provider not found by formatId: " + provider.getFormatId());
            }
        }
        if (!formats.equals(java.util.Set.of("XML", "JSON", "YAML"))) {
            throw new AssertionError("Missing or unexpected ServiceLoader providers: " + formats);
        }
        // Registering additional discovery scopes cannot produce duplicate
        // providers for the same actual Java implementation class.
        var loader = AItcSmartDataObjectFormatRoundtripTest.class.getClassLoader();
        AIsSmartDataObjectFormatBindingProviderRegistry.registerClassLoader(loader);
        AIsSmartDataObjectFormatBindingProviderRegistry.registerModuleLayer(ModuleLayer.boot());
        try {
            var after = AIsSmartDataObjectFormatBindingProviderRegistry.getProviders();
            if (after.size() != providers.size()) {
                throw new AssertionError("Duplicated providers across discovery scopes");
            }
        } finally {
            AIsSmartDataObjectFormatBindingProviderRegistry.unregisterModuleLayer(ModuleLayer.boot());
            AIsSmartDataObjectFormatBindingProviderRegistry.unregisterClassLoader(loader);
        }
        System.out.println("OK: ServiceLoader discovery and XML/JSON/YAML SDO roundtrips");
    }
}
