package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.data.dataobject.xmldefs.AIiXmlDataObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Regression tests for stable XML field ordering. Java reflection does not
 * specify getter declaration order, so the XML annotation carries the
 * zero-based schema position while the SmartData metadata scanner sorts
 * members within their declaring read contract before constructing a graph.
 */
public final class AItcXmlSmartDataObjectFieldOrderTest {
    /**
     * Example schema contract with method declarations intentionally reversed
     * relative to their XML positions.
     */
    public interface AIiOrderedExample extends AIiXmlDataObject {
        /** Later XML field, listed first to prevent accidental order assumptions. */
        @AIaDataObjectField(fieldName = "second")
        @AIaXmlDataObjectField(namespaceLocalName = "Second", order = 1)
        String getSecond();

        /** First XML field, listed second to require numeric ordering. */
        @AIaDataObjectField(fieldName = "first")
        @AIaXmlDataObjectField(namespaceLocalName = "First", order = 0)
        String getFirst();
    }

    /**
     * Confirms that XML field metadata restores the original schema order
     * independently of the JVM's reflection method enumeration.
     */
    @Test
    public void AIcOrderXmlContractGettersBySchemaPosition() {
        var locFields = AIcSmartDataMetadata.forReadContract(AIiOrderedExample.class).fields();
        Assert.assertEquals(locFields.get(0).name(), "first");
        Assert.assertEquals(locFields.get(1).name(), "second");
        Assert.assertEquals(locFields.get(0).xml().order(), 0);
        Assert.assertEquals(locFields.get(1).xml().order(), 1);
    }
}
