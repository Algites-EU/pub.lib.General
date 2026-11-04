package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.AIiDataObjectIntegratedAdditionalPropertiesMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies the live additional-property map and the strict separation from
 * declared, inherited and schema-metadata property names.
 *
 * @author linhart1
 */
public final class AItcIntegratedAdditionalPropertiesMapTest {

    /** Base schema with an intentionally different semantic getter and wire name. */
    @AIaDataObject
    public interface AIigBase extends AIiDataObject {
        /** @return the declared name */
        @AIaDataObjectField(fieldName = "Name")
        String getPersonName();
    }

    /** Schema declaring a second fixed property and a metadata property. */
    @AIaDataObject(dataSourceSchemaUrlField = "$schema")
    public interface AIigExample extends AIigBase {
        /** @return the declared age */
        @AIaDataObjectField(fieldName = "Age")
        Integer getAge();

        /** @return the live additional-property map, excluding both fixed fields */
        AIiDataObjectIntegratedAdditionalPropertiesMap<Boolean> getAdditionalProperties();
    }

    /** Mutable SDO test implementation, as it would be generated. */
    public static final class AIcgdExample extends AIcSmartDataObject implements AIigExample {
        /** Constructs an empty example. */
        public AIcgdExample() { super(); }

        /** {@inheritDoc} */
        @Override public String getPersonName() { return (String) get_EffectiveField("PersonName"); }

        /** {@inheritDoc} */
        @Override public Integer getAge() { return (Integer) get_EffectiveField("Age"); }

        /** {@inheritDoc} */
        @Override public AIiDataObjectIntegratedAdditionalPropertiesMap<Boolean> getAdditionalProperties() {
            return get_IntegratedAdditionalPropertiesMap(Boolean.class);
        }

        /** Assigns the fixed Name field. @param aName new name */
        public void setPersonName(String aName) { set_RawField("PersonName", aName); }

        /** Unsets the fixed Name field. */
        public void unsetPersonName() { unset_Field("PersonName"); }
    }

    /** A live map must not reveal fixed fields even when they are present. */
    @Test public void AIcDeclaredAndAdditionalPropertiesStayDisjoint() {
        AIcgdExample locObject = new AIcgdExample();
        locObject.setPersonName("John");
        var locMap = locObject.getAdditionalProperties();
        Assert.assertSame(locMap, locObject.getAdditionalProperties());
        Assert.assertNull(locMap.get("Name"));
        Assert.assertFalse(locMap.containsKey("Name"));
        Assert.assertNull(locMap.remove("Name"));
        Assert.assertEquals(locObject.getPersonName(), "John");
        Assert.assertEquals(locMap.put("Enabled", true), null);
        Assert.assertEquals(locMap.get("Enabled"), Boolean.TRUE);
        Assert.assertEquals(locMap.size(), 1);
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.put("Name", false));
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.put("Age", false));
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.put("$schema", false));
        locObject.unsetPersonName();
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.put("Name", false));
        Assert.assertTrue(locMap.containsKey("Enabled"));
        Assert.assertEquals(locMap.size(), 1);
    }

    /** Bulk failure must be atomic and null value should remain distinguishable. */
    @Test public void AIcBulkMutationAndNullValues() {
        var locMap = new AIcgdExample().getAdditionalProperties();
        locMap.put("Nullable", null);
        Assert.assertTrue(locMap.containsKey("Nullable"));
        Assert.assertNull(locMap.get("Nullable"));
        Map<String, Boolean> locValues = new LinkedHashMap<>();
        locValues.put("Allowed", true);
        locValues.put("Name", false);
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.putAll(locValues));
        Assert.assertFalse(locMap.containsKey("Allowed"));
        Assert.expectThrows(NullPointerException.class, () -> locMap.put(null, true));
        locMap.put("", true); // The empty key is legal JSON; empty metadata settings are not reservations.
        Assert.assertEquals(locMap.get(""), Boolean.TRUE);
    }

    /** Entries, iterators and default Map mutation methods must retain live state. */
    @Test public void AIcEntryAndDefaultMapMutations() {
        var locMap = new AIcgdExample().getAdditionalProperties();
        locMap.put("Enabled", true);
        var locEntry = locMap.entrySet().iterator().next();
        Assert.assertEquals(locEntry.setValue(false), Boolean.TRUE);
        Assert.assertEquals(locMap.get("Enabled"), Boolean.FALSE);
        Assert.expectThrows(IllegalArgumentException.class, () -> locMap.computeIfAbsent("Name", k -> true));
        Assert.assertEquals(locMap.computeIfAbsent("Other", k -> true), Boolean.TRUE);
        Iterator<Map.Entry<String, Boolean>> locIterator = locMap.entrySet().iterator();
        locIterator.next();
        locIterator.remove();
        Assert.assertEquals(locMap.size(), 1);
        locMap.clear();
        Assert.assertTrue(locMap.isEmpty());
    }
}
