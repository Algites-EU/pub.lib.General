package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.*;

import eu.algites.lib.data.smartdataobject.impl.common.*;
import java.util.*;

/** Smoke regression for typed metadata, inherited contracts and per-registry isolation.
 * Run its {@link #main(String[])} without a test framework.
 * @author linhart1
 */
public final class AItcSmartDataObjectBindingRegistryTest {
    interface Address extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="postal_code")
        String getPostalCode();
    }
    interface Contact extends AIiDataObject { }
    interface Empty extends AIiDataObject { }
    @AIaSmartDataObjectImplementationBinding(readContracts={Empty.class})
    static class EmptySDO extends AIcSmartDataObject implements Empty { }
    @AIaSmartDataObjectImplementationBinding(readContracts={Address.class})
    static class AddressSDO extends AIcSmartDataObject implements Address {
        public String getPostalCode() { return "100"; }
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Contact.class})
    static class CustomAddressSDO extends AddressSDO implements Contact {
        @Override public String getPostalCode() { return "200"; }
    }
    interface Person extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="address")
        Address getAddress();
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Person.class})
    static class PersonSDO extends AIcSmartDataObject implements Person {
        @AIaSmartDataObjectFieldImplementationBinding(implementationClass=CustomAddressSDO.class)
        public Address getAddress() { return null; }
    }
    /**
     * Executes the metadata and binding regression assertions.
     * @param args unused command-line arguments
     * @throws AssertionError when a binding invariant is violated
     */
    public static void main(String[] args) {
        if (!AIsSmartDataObjectMetadataRegistry.forType(EmptySDO.class).getFields().isEmpty())
            throw new AssertionError("empty SDO contract");
        var emptyRegistry = AIcSmartDataObjectBindingRegistry.builder().register(EmptySDO.class).build();
        if (emptyRegistry.getBindingsForReadContract(Empty.class).size() != 1)
            throw new AssertionError("empty SDO binding");
        AIiSmartDataObjectMetadata md = AIsSmartDataObjectMetadataRegistry.forType(CustomAddressSDO.class);
        if(md.getFields().size()!=1) throw new AssertionError(md.getFields().toString());
        if(!md.getField("PostalCode").getDataFieldName().equals("postal_code")) throw new AssertionError("data name");
        if(!md.getField("PostalCode").getEffectiveGetter().getDeclaringClass().equals(CustomAddressSDO.class))
            throw new AssertionError("missing override getter");
        var r = AIcSmartDataObjectBindingRegistry.builder()
              .register(AddressSDO.class).register(CustomAddressSDO.class).register(PersonSDO.class).build();
        if(r.getBindingsForReadContract(Address.class).size()!=2) throw new AssertionError("candidate count");
        var item = r.getBinding(CustomAddressSDO.class);
        if(!item.getDeclaredReadContracts().contains(Contact.class)) throw new AssertionError("declared");
        if(!item.getEffectiveReadContracts().contains(Address.class)) throw new AssertionError("inherited");
        if(!r.getBinding(PersonSDO.class).getFieldBinding("Address").getImplementationClassOverride()
                .orElseThrow().equals(CustomAddressSDO.class)) throw new AssertionError("field override");
        var restricted = AIcSmartDataObjectBindingRegistry.builder().register(AddressSDO.class).build();
        if(restricted.getBindingsForReadContract(Contact.class).size()!=0) throw new AssertionError("scope leak");
        System.out.println("OK: metadata, inherited contracts, multiple implementations, scoped registry, field override");
    }
}
