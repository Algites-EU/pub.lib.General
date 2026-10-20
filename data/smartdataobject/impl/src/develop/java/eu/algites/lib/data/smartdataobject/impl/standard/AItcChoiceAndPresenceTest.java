package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Checks ordinary interface inheritance and derived field-presence restrictions. */
public final class AItcChoiceAndPresenceTest {
    @AIaDataObject
    public interface AIigPayment extends AIiDataObject { }

    @AIaDataObject
    public interface AIigCryptoPayment extends AIigPayment { }

    @AIaDataObject
    public interface AIigTokenPayment extends AIigCryptoPayment { }

    @AIaDataObject
    public interface AIigUnregisteredPayment extends AIigPayment { }

    @AIaDataObject()
    public interface AIigdTokenPayment extends AIigTokenPayment { }
    public record AIcgdRealisticTokenPayment() implements AIigdTokenPayment { }

    public record AIcdTokenPayment() implements AIigTokenPayment { }
    public record AIcdUnregisteredPayment() implements AIigUnregisteredPayment { }
    @AIaDataObject
    public interface AIigUnregisteredTokenExtension extends AIigTokenPayment { }
    public record AIcdUnregisteredTokenExtension() implements AIigUnregisteredTokenExtension { }

    @AIaDataObject
    public interface AIigBase extends AIiDataObject {
        @AIaDataObjectField(fieldName = "Reference")
        String getReference();
    }

    @AIaDataObject
    public interface AIigRestricted extends AIigBase {
        @Override
        @AIaDataObjectField(fieldName = "Reference", presence = AInFieldPresenceKind.FORBIDDEN)
        String getReference();
    }

    public static final class AIcgdRestricted extends AIcSmartDataObject implements AIigRestricted {
        @Override public String getReference() { return (String) get_EffectiveField("Reference"); }
        public void setReference(String aValue) { set_RawField("Reference", aValue); }
        public String get_Reference() { return (String) get_RawField("Reference"); }
    }

    @AIaDataObject
    public interface AIigOwner extends AIiDataObject {
        @AIaDataObjectField(fieldName = "Payment")
        AIigPayment getPayment();
    }

    public static final class AIcgdOwner extends AIcSmartDataObject implements AIigOwner {
        @Override public AIigPayment getPayment() { return (AIigPayment) get_EffectiveField("Payment"); }
        public void setPayment(AIigPayment aPayment) { set_RawField("Payment", aPayment); }
    }

    /** Substitution relationships use Java interfaces rather than runtime OpenChoice metadata. */
    @Test public void AIcXmlElementSubstitutionByInheritance() {
        Assert.assertTrue(AIigPayment.class.isAssignableFrom(AIigTokenPayment.class));
        Assert.assertTrue(AIigPayment.class.isAssignableFrom(AIigUnregisteredPayment.class));
        AIcgdOwner locOwner = new AIcgdOwner();
        locOwner.setPayment(new AIcdTokenPayment());
        locOwner.setPayment(new AIcdUnregisteredPayment());
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
