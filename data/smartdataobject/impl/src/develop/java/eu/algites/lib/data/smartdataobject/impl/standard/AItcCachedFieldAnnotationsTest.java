package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldStringConstraints;
import eu.algites.lib.data.dataobject.AIaKeyDefinition;
import eu.algites.lib.data.dataobject.AIiDataObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies that runtime field annotations are resolved and cached with the
 * semantic field, including inherited constraints and indexed-list keys.
 *
 * @author linhart1
 */
public final class AItcCachedFieldAnnotationsTest {
    /** Base contract with metadata that must survive covariant overrides. */
    public interface AIigBase extends AIiDataObject {
        /** @return schema-constrained string code */
        @AIaDataObjectField(fieldName = "code")
        @AIaDataObjectFieldStringConstraints(minLength = 3)
        String getCode();

        /** @return field with one named key definition */
        @AIaDataObjectField(fieldName = "items")
        @AIaKeyDefinition(name = "byCode", fields = {"Code"})
        java.util.List<String> getItemsList();
    }

    /** Specialized contract that inherits metadata but redeclares the getter. */
    public interface AIigDerived extends AIigBase {
        /** @return string code without re-declaring the string constraints */
        @Override @AIaDataObjectField
        String getCode();
    }

    /** Minimal mutable implementation backed by metadata descriptors. */
    public static final class AIcDerived extends AIcSmartDataObject implements AIigDerived {
        /** {@inheritDoc} */
        @Override public String getCode() { return (String) get_EffectiveField("Code"); }
        /** {@inheritDoc} */
        @Override public java.util.List<String> getItemsList() { return (java.util.List<String>) get_EffectiveField("ItemsList"); }
        /** @param value code to validate before assignment */
        public void setCode(String value) { set_RawField("Code", value); }
    }

    /** Cache all restrictions and index annotations once per implementation. */
    @Test
    public void metadataCachesEffectiveAnnotations() {
        var first = AIcSmartDataMetadata.forType(AIcDerived.class);
        Assert.assertSame(first, AIcSmartDataMetadata.forType(AIcDerived.class));
        var code = first.names().get("Code");
        Assert.assertNotNull(code);
        Assert.assertNotNull(code.stringConstraints());
        Assert.assertEquals(code.stringConstraints().minLength(), 3);
        Assert.assertEquals(code.wireName(), "code");
        var indexed = first.names().get("ItemsList");
        Assert.assertNotNull(indexed);
        Assert.assertEquals(indexed.keyDefinitions().size(), 1);
        Assert.assertEquals(indexed.keyDefinitions().get(0).name(), "byCode");
        try {
            new AIcDerived().setCode("ab");
            Assert.fail("Expected inherited minimum-length restriction");
        } catch (IllegalArgumentException expected) {
            Assert.assertTrue(expected.getMessage().contains("String constraint"));
        }
        var value = new AIcDerived();
        value.setCode("abc");
        Assert.assertEquals(value.getCode(), "abc");
    }
}
