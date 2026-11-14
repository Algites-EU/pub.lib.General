package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.common.list.AIiMultiMappedList;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaKeyDefinition;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Regression for occurrence-scoped XML keys on one shared named XSD TYPE.
 *
 * <p>An identity constraint decorates a referencing element; it never clones
 * or mutates the shared TYPE. Both references have the same Java implementation
 * class, but only the keyed occurrence has a live index.</p>
 *
 * @author linhart1
 */
public final class AItcXmlScopedNamedTypeKeyTest {
  /** One shared list item declaration. */
  public interface Entry extends AIiXmlDataObject {
    /** @return name attribute used as the key field */
    @AIaDataObjectField(fieldName="Name")
    @AIaXmlDataObjectField(namespaceLocalName="name",valueKind=AInXmlValueKind.ATTRIBUTE)
    String getName();
  }
  /** One reusable nominal container TYPE. */
  public interface Entries extends AIiXmlDataObject {
    /** @return repeated child items, potentially indexed per occurrence */
    @AIaDataObjectField(fieldName="Entry")
    @AIaXmlDataObjectField(namespaceLocalName="Entry")
    AIiMultiMappedList<Entry> getEntry();
  }
  /** Two occurrences of the same declared container TYPE. */
  public interface Root extends AIiXmlDataObject {
    /** @return specifically indexed occurrence of the shared TYPE */
    @AIaDataObjectField(fieldName="Keyed")
    @AIaXmlDataObjectField(namespaceLocalName="Keyed")
    @AIaKeyDefinition(name="ByName",selector="Entry",fields={"Name"})
    Entries getKeyed();
    /** @return unindexed occurrence of the same shared TYPE */
    @AIaDataObjectField(fieldName="Unkeyed")
    @AIaXmlDataObjectField(namespaceLocalName="Unkeyed")
    Entries getUnkeyed();
  }
  /** XML parser implementation of the declared item TYPE. */
  public static final class EntryImpl extends AIcXmlSmartDataObject implements Entry {
    /** Constructs an empty item. */
    public EntryImpl() {}
    /** {@inheritDoc} */
    public String getName() {return (String)get_RawField("Name");}
  }
  /** XML parser implementation of the declared container TYPE. */
  public static final class EntriesImpl extends AIcXmlSmartDataObject implements Entries {
    /** Constructs an empty container. */
    public EntriesImpl() {}
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") public AIiMultiMappedList<Entry> getEntry() {
        return (AIiMultiMappedList<Entry>)get_RawField("Entry");}
    /** @return generated parser mappings for nested values */
    public static Map<String,Class<? extends AIcXmlSmartDataObject>> xmlNestedTypes(){return Map.of("Entry",EntryImpl.class);}
  }
  /** XML parser implementation of the declared root TYPE. */
  public static final class RootImpl extends AIcXmlSmartDataObject implements Root {
    /** Constructs an empty root. */
    public RootImpl() {}
    /** {@inheritDoc} */
    public Entries getKeyed(){return (Entries)get_RawField("Keyed");}
    /** {@inheritDoc} */
    public Entries getUnkeyed(){return (Entries)get_RawField("Unkeyed");}
    /** @return generated parser mappings for nested values */
    public static Map<String,Class<? extends AIcXmlSmartDataObject>> xmlNestedTypes(){return Map.of("Keyed",EntriesImpl.class,"Unkeyed",EntriesImpl.class);}
  }
  /**
   * Ensure only the locally constrained occurrence gets an index, while an
   * unconstrained occurrence of the same nominal TYPE can contain duplicates.
   */
  @Test
  public void indexesOnlyTheSelectedOccurrence() {
    var root=AIcXmlSmartDataObject.fromXml("<Root><Keyed><Entry name='A'/><Entry name='B'/></Keyed><Unkeyed><Entry name='A'/><Entry name='A'/></Unkeyed></Root>",RootImpl.class,Map.of());
    Assert.assertNotNull(root.getKeyed().getEntry().keyDefinition("ByName"));
    Assert.assertNull(root.getUnkeyed().getEntry().keyDefinition("ByName"));
    Assert.assertEquals(root.getUnkeyed().getEntry().size(), 2);
    Assert.assertEquals(root.getKeyed().getEntry().mapByKeyId("ByName").get("A").size(), 1);
    Assert.assertSame(root.getKeyed().getClass(), root.getUnkeyed().getClass());
    Assert.expectThrows(IllegalArgumentException.class,
        () -> AIcXmlSmartDataObject.fromXml(
            "<Root><Keyed><Entry name='A'/><Entry name='A'/></Keyed></Root>",
            RootImpl.class, Map.of()));
  }
}
