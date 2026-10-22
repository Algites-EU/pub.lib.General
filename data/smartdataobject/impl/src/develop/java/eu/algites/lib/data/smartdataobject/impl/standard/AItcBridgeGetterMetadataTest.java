package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Regression tests for compiler-generated bridge getters in covariant SDO contracts.
 * Java copies method annotations onto bridge methods, but such methods do not
 * introduce additional field definitions and must not narrow the same field twice.
 */
public final class AItcBridgeGetterMetadataTest {
    /** Base read-only object contract. */
    public interface AIigPayload extends AIiDataObject { }

    /** Writable payload subtype used to narrow the getter. */
    public interface AIigdPayload extends AIigPayload { }

    /** Base container exposing the read-only payload. */
    @AIaDataObject(schemaId = "test:bridge-read", schemaVersion = 1)
    public interface AIigBox extends AIiDataObject {
        /** Return the payload through its read-only contract. */
        @AIaDataObjectField(fieldName = "Payload")
        AIigPayload getPayload();
    }

    /** Writable container with a generic, covariantly specialized getter. */
    @AIaDataObject(schemaId = "test:bridge-write", schemaVersion = 1)
    public interface AIigdBox<TPayload extends AIigdPayload> extends AIigBox {
        /** Return the exact writable payload subtype. */
        @Override
        @AIaDataObjectField(fieldName = "Payload")
        TPayload getPayload();
    }

    /** Concrete payload with no additional fields. */
    public static final class AIcPayload implements AIigdPayload { }

    /** Concrete SmartDataObject forwarding its typed setter to the runtime. */
    public static final class AIcBox extends AIcSmartDataObject implements AIigdBox<AIcPayload> {
        /** Read the effective payload. */
        @Override
        public AIcPayload getPayload() {
            return (AIcPayload) get_EffectiveField("Payload");
        }

        /** Assign a payload of the specialized writable type. */
        public void setPayload(AIcPayload aValue) {
            set_RawField("Payload", aValue);
        }
    }

    /** Confirm that the bridge does not supersede the actual covariant declaration. */
    @Test
    public void AIcRuntimeIgnoresAnnotatedBridgeGetters() {
        Assert.assertTrue(java.util.Arrays.stream(AIigdBox.class.getDeclaredMethods())
                .anyMatch(aMethod -> aMethod.isBridge() && aMethod.isAnnotationPresent(AIaDataObjectField.class)),
                "Javac must emit the annotated bridge exercised by this regression test");
        var locPayload = new AIcPayload();
        var locBox = new AIcBox();
        locBox.setPayload(locPayload);
        Assert.assertSame(locBox.getPayload(), locPayload);
        Assert.assertEquals(AIcSmartDataMetadata.forType(AIcBox.class).names()
                .get("Payload").getter().getReturnType(), AIigdPayload.class);
    }
}
