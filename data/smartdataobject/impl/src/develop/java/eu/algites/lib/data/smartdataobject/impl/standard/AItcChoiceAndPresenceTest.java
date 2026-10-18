package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaOpenChoiceTypeOf;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;
import eu.algites.lib.data.dataobject.AIsOpenChoiceTypes;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Checks explicit transitive substitutions and derived field-presence restrictions. */
public final class AItcChoiceAndPresenceTest {
    @AIaDataObject
    public interface AIigPayment extends AIiDataObject { }

    @AIaDataObject
    @AIaOpenChoiceTypeOf(AIigPayment.class)
    public interface AIigCryptoPayment extends AIigPayment { }

    @AIaDataObject
    @AIaOpenChoiceTypeOf(AIigCryptoPayment.class)
    public interface AIigTokenPayment extends AIigCryptoPayment { }

    @AIaDataObject
    public interface AIigUnregisteredPayment extends AIigPayment { }

    @AIaDataObject(sourceContract = AIigTokenPayment.class)
    public interface AIigdTokenPayment extends AIigTokenPayment { }
    public record AIcgdRealisticTokenPayment() implements AIigdTokenPayment { }

    public record AIcdTokenPayment() implements AIigTokenPayment { }
    public record AIcdUnregisteredPayment() implements AIigUnregisteredPayment { }
    @AIaDataObject
    public interface AIigUnregisteredTokenExtension extends AIigTokenPayment { }
    public record AIcdUnregisteredTokenExtension() implements AIigUnregisteredTokenExtension { }

    @AIaDataObject
    public interface AIigBase extends AIiDataObject {
        @AIaDataObjectField(name = "Reference")
        String getReference();
    }

    @AIaDataObject
    public interface AIigRestricted extends AIigBase {
        @Override
        @AIaDataObjectField(name = "Reference", presence = AInFieldPresenceKind.FORBIDDEN)
        String getReference();
    }

    public static final class AIcgdRestricted extends AIcSmartDataObject implements AIigRestricted {
        @Override public String getReference() { return (String) get_EffectiveField("Reference"); }
        public void setReference(String aValue) { set_RawField("Reference", aValue); }
        public String get_Reference() { return (String) get_RawField("Reference"); }
    }

    @AIaDataObject
    public interface AIigOwner extends AIiDataObject {
        @AIaDataObjectField(name = "Payment", openChoice = true)
        AIigPayment getPayment();
    }

    public static final class AIcgdOwner extends AIcSmartDataObject implements AIigOwner {
        @Override public AIigPayment getPayment() { return (AIigPayment) get_EffectiveField("Payment"); }
        public void setPayment(AIigPayment aPayment) { set_RawField("Payment", aPayment); }
    }

    /** Compile-time Java subtyping plus runtime explicitly authorized OpenChoice edges. */
    @Test public void AIcOpenChoiceTransitivityAndEnforcement() {
        Assert.assertTrue(AIsOpenChoiceTypes.allowedHeads(AIigTokenPayment.class).contains(AIigPayment.class));
        Assert.assertTrue(AIsOpenChoiceTypes.permits(new AIcdTokenPayment(), AIigPayment.class));
        Assert.assertTrue(AIsOpenChoiceTypes.permits(new AIcgdRealisticTokenPayment(), AIigPayment.class));
        Assert.assertFalse(AIsOpenChoiceTypes.permits(new AIcdUnregisteredPayment(), AIigPayment.class));
        Assert.assertFalse(AIsOpenChoiceTypes.permits(new AIcdUnregisteredTokenExtension(), AIigPayment.class));
        AIcgdOwner locOwner = new AIcgdOwner();
        locOwner.setPayment(new AIcdTokenPayment());
        Assert.expectThrows(IllegalArgumentException.class,
                () -> locOwner.setPayment(new AIcdUnregisteredPayment()));
    }

    /** Narrowed field may not be read, assigned or introduced via public SDO API. */
    @Test public void AIcRestrictedFieldOverridesParent() {
        AIcgdRestricted locRestricted = new AIcgdRestricted();
        Assert.assertTrue(locRestricted.is_ByRawValuesValid());
        Assert.expectThrows(IllegalStateException.class, () -> locRestricted.setReference("X"));
        Assert.expectThrows(IllegalStateException.class, locRestricted::getReference);
        Assert.expectThrows(IllegalStateException.class, locRestricted::get_Reference);
    }
}
