package eu.algites.lib.data.smartdataobject.impl.format.xml;

import java.util.*;
import eu.algites.lib.common.list.AIiMultiMappedList;
import eu.algites.lib.data.dataobject.*;
import eu.algites.lib.data.dataobject.xmldefs.*;
import eu.algites.lib.data.smartdataobject.impl.common.*;
import eu.algites.lib.data.smartdataobject.impl.format.xml.*;

/**
 * Java XML ELEMENT round-trip with a nested typed keyed AIiMultiMappedList.
 * Exercises the independent SDO factory and list cardinality/index preservation.
 */
public final class AItcSmartDataObjectXmlIndexedRootTest {
    @AIaDataObjectXmlSchema(definitionKind=AInXmlObjectDefinitionKind.ELEMENT,namespace="urn:test",namespaceLocalName="Persons")
    @AIaDataObject(description="Persons")
    public interface Persons extends AIiDataObject {
        @AIaDataObjectListKeyDefinition(name="UniquePersonId", fields={"Id"})
        @AIaDataObjectField(dataFieldName="Person", minItemCount=0, maxItemCount=-1)
        @AIaDataObjectFieldXmlSchema(namespace="urn:test", namespaceLocalName="Person")
        AIiMultiMappedList<? extends Person> getPersonList();
    }
    @AIaDataObjectXmlSchema(definitionKind=AInXmlObjectDefinitionKind.TYPE,namespace="urn:test",namespaceLocalName="PersonType")
    @AIaDataObject(description="Person")
    public interface Person extends AIiDataObject {
        @AIaDataObjectField(dataFieldName="Id", presence=AInFieldPresenceKind.REQUIRED)
        @AIaDataObjectFieldXmlSchema(namespaceLocalName="Id",valueKind=AInXmlValueKind.ATTRIBUTE)
        String getId();
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Persons.class})
    public static class PersonsSdo extends AIcSmartDataObject implements Persons {
        public PersonsSdo() {}
        @Override public AIiMultiMappedList<? extends Person> getPersonList() {
            return (AIiMultiMappedList<? extends Person>) get_OrCreateCollectionField("PersonList",true);
        }
    }
    @AIaSmartDataObjectImplementationBinding(readContracts={Person.class})
    public static class PersonSdo extends AIcSmartDataObject implements Person {
        public PersonSdo() {}
        @Override public String getId(){ return (String)get_EffectiveField("Id"); }
        public void setId(String id){set_RawField("Id",id);}
    }
    static void check(boolean ok,String what){if(!ok)throw new AssertionError(what);}
    public static void main(String[] ignored) {
        var registry=AIcSmartDataObjectBindingRegistry.builder().register(PersonsSdo.class).register(PersonSdo.class).build();
        var result=AIsSmartDataObjectFactory.fromData(Persons.class, Map.of("Person",List.of(Map.of("Id","alice"))),registry);
        check(result.getPersonList().size()==1,"factory list size");
        check(result.getPersonList().getKeyCount()==1,"registered XML key");
        check(result.getPersonList().<String>getOneKeyMap().get("alice").size()==1,"indexed key lookup");
        check(result.getPersonList().get(0).getId().equals("alice"),"factory child type");
        System.out.println("PASS Java factory MultiMappedList with typed child");
        var provider=new AIcXmlFormatBindingProvider();
        var serialized=provider.createMarshaller().marshal((PersonsSdo)result,registry);
        check(serialized.contains("Persons")&&serialized.contains("alice"),"serialized XML");
        var restored=(Persons)provider.createUnmarshaller().unmarshal(serialized,Persons.class,registry);
        check(restored.getPersonList().size()==1,"roundtrip list size");
        check(restored.getPersonList().<String>getOneKeyMap().get("alice").size()==1,"restored indexed key lookup");
        check(restored.getPersonList().get(0).getId().equals("alice"),"roundtrip identity");
        System.out.println("PASS Java XML global ELEMENT MultiMappedList round-trip: "+serialized);
    }
}
