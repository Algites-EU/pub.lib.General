package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.common.list.AIiMultiMappedList;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlKeyDefinition;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind;
import eu.algites.lib.data.smartdataobject.impl.xmldefs.AIcXmlSmartDataObject;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies local {@code xs:key} materialization and independent indices across
 * nested XML collections with the same method metadata emitted by Defs/SDO Codegen.
 *
 * @author linhart1
 */
public final class AItcXmlLocalKeyedCollectionTest {
    /** Schema contract for the company containing repeating departments. */
    public interface AIiCompany extends AIiXmlDataObject {
        /** @return all departments in XML document order */
        @AIaDataObjectField(fieldName = "Department")
        @AIaXmlDataObjectField(namespace = "urn:test", namespaceLocalName = "Department",
                minOccurs = 1, maxOccurs = -1)
        List<? extends AIiDepartment> getDepartment();
    }

    /** Schema contract whose employee list has two independent unique indexes. */
    public interface AIiDepartment extends AIiXmlDataObject {
        /** @return employees local to this department, keyed by identity and region/code */
        @AIaDataObjectField(fieldName = "Employee")
        @AIaXmlDataObjectField(namespace = "urn:test", namespaceLocalName = "Employee",
                minOccurs = 1, maxOccurs = -1)
        @AIaXmlKeyDefinition(name = "ById", fields = {"id"})
        @AIaXmlKeyDefinition(name = "ByRegionCode", fields = {"region", "Identity.Code"})
        AIiMultiMappedList<AIiEmployee> getEmployee();

        /** @return optional department name from an unqualified attribute */
        @AIaDataObjectField(fieldName = "Name")
        @AIaXmlDataObjectField(namespaceLocalName = "name", valueKind = AInXmlValueKind.ATTRIBUTE)
        String getName();
    }

    /** Schema contract for one employee and its nested typed identity object. */
    public interface AIiEmployee extends AIiXmlDataObject {
        /** @return mandatory nested identity object */
        @AIaDataObjectField(fieldName = "Identity")
        @AIaXmlDataObjectField(namespace = "urn:test", namespaceLocalName = "Identity")
        AIiIdentity getIdentity();

        /** @return mandatory, unqualified XML ID attribute */
        @AIaDataObjectField(fieldName = "Id")
        @AIaXmlDataObjectField(namespaceLocalName = "id", valueKind = AInXmlValueKind.ATTRIBUTE)
        String getId();

        /** @return region segment of the compound key */
        @AIaDataObjectField(fieldName = "Region")
        @AIaXmlDataObjectField(namespaceLocalName = "region", valueKind = AInXmlValueKind.ATTRIBUTE)
        String getRegion();
    }

    /** Schema contract for the numeric part of an employee's compound key. */
    public interface AIiIdentity extends AIiXmlDataObject {
        /** @return typed integer code, not its original textual XML spelling */
        @AIaDataObjectField(fieldName = "Code")
        @AIaXmlDataObjectField(namespace = "urn:test", namespaceLocalName = "Code")
        BigInteger getCode();
    }

    /** Generated-style concrete company with a local nested-type registry. */
    public static final class AIcCompany extends AIcXmlSmartDataObject implements AIiCompany {
        /** Constructs the initially empty root. */
        public AIcCompany() { super(); }
        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public List<? extends AIiDepartment> getDepartment() {
            return (List<? extends AIiDepartment>) get_EffectiveField("Department");
        }
        /** @return department implementation mapping for the XML parser */
        public static Map<String, Class<? extends AIcXmlSmartDataObject>> xmlNestedTypes() {
            return Map.of("Department", AIcDepartment.class);
        }
    }

    /** Generated-style concrete department with per-instance keyed employees. */
    public static final class AIcDepartment extends AIcXmlSmartDataObject implements AIiDepartment {
        /** Constructs an initially empty department. */
        public AIcDepartment() { super(); }
        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public AIiMultiMappedList<AIiEmployee> getEmployee() {
            return (AIiMultiMappedList<AIiEmployee>) get_EffectiveField("Employee");
        }
        /** {@inheritDoc} */
        @Override public String getName() { return (String) get_EffectiveField("Name"); }
        /** @return employee implementation mapping for the XML parser */
        public static Map<String, Class<? extends AIcXmlSmartDataObject>> xmlNestedTypes() {
            return Map.of("Employee", AIcEmployee.class);
        }
    }

    /** Generated-style concrete employee with stable typed key components. */
    public static final class AIcEmployee extends AIcXmlSmartDataObject implements AIiEmployee {
        /** Constructs an initially empty employee. */
        public AIcEmployee() { super(); }
        /** {@inheritDoc} */
        @Override public AIiIdentity getIdentity() { return (AIiIdentity) get_EffectiveField("Identity"); }
        /** {@inheritDoc} */
        @Override public String getId() { return (String) get_EffectiveField("Id"); }
        /** {@inheritDoc} */
        @Override public String getRegion() { return (String) get_EffectiveField("Region"); }
        /** @return nested identity implementation mapping for the XML parser */
        public static Map<String, Class<? extends AIcXmlSmartDataObject>> xmlNestedTypes() {
            return Map.of("Identity", AIcIdentity.class);
        }
    }

    /** Generated-style concrete typed identity for a composite index. */
    public static final class AIcIdentity extends AIcXmlSmartDataObject implements AIiIdentity {
        /** Constructs an initially empty identity. */
        public AIcIdentity() { super(); }
        /** {@inheritDoc} */
        @Override public BigInteger getCode() { return (BigInteger) get_EffectiveField("Code"); }
    }

    /** XML fixture with identical IDs in different departments, which is valid. */
    private static final String VALID = "<Company xmlns='urn:test'>"
            + "<Department name='Development'>"
            + "<Employee id='101' region='eu'><Identity><Code>3</Code></Identity></Employee>"
            + "<Employee id='102' region='us'><Identity><Code>003</Code></Identity></Employee>"
            + "</Department><Department name='Sales'>"
            + "<Employee id='101' region='eu'><Identity><Code>3</Code></Identity></Employee>"
            + "</Department></Company>";

    /** Tests per-owner indexes, compound typed values and live map lookups. */
    @Test
    public void parsesIndependentTypedIndexes() {
        AIcCompany company = AIcXmlSmartDataObject.fromXml(VALID, AIcCompany.class, Map.of());
        Assert.assertEquals(company.getDepartment().size(), 2);
        AIiMultiMappedList<AIiEmployee> development = company.getDepartment().get(0).getEmployee();
        AIiMultiMappedList<AIiEmployee> sales = company.getDepartment().get(1).getEmployee();
        Assert.assertEquals(development.size(), 2);
        Assert.assertEquals(sales.size(), 1);
        Assert.assertNotNull(development.keyDefinition("ById"));
        Assert.assertNotNull(development.keyDefinition("ByRegionCode"));
        Assert.assertNotSame(development.keyDefinition("ById"), sales.keyDefinition("ById"));
        Assert.assertEquals(development.mapByKeyType(development.keyDefinition("ById"))
                .get("101").get(0).getId(), "101");
        Assert.assertEquals(development.mapByKeyType(development.keyDefinition("ByRegionCode"))
                .get(List.of("us", BigInteger.valueOf(3))).get(0).getId(), "102");
        Assert.assertEquals(development.get(1).getIdentity().getCode(), BigInteger.valueOf(3));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> development.add(development.get(0)));
        Assert.assertEquals(development.size(), 2);
        Assert.assertEquals(development.mapByKeyType(development.keyDefinition("ById")).size(), 2);
    }

    /** Tests collision rejection for either unique index with the other unchanged. */
    @Test
    public void rejectsDuplicatesWithinOneDepartment() {
        String compositeCollision = VALID.replace("id='102' region='us'", "id='102' region='eu'");
        String idCollision = VALID.replace("id='102' region='us'", "id='101' region='us'");
        Assert.expectThrows(IllegalArgumentException.class,
                () -> AIcXmlSmartDataObject.fromXml(compositeCollision, AIcCompany.class, Map.of()));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> AIcXmlSmartDataObject.fromXml(idCollision, AIcCompany.class, Map.of()));
    }

    /** Tests that an absent nested mandatory component cannot enter an {@code xs:key}. */
    @Test
    public void rejectsMissingNestedKeyComponent() {
        String missing = VALID.replaceFirst("<Identity><Code>3</Code></Identity>", "");
        Assert.expectThrows(IllegalArgumentException.class,
                () -> AIcXmlSmartDataObject.fromXml(missing, AIcCompany.class, Map.of()));
    }
}
