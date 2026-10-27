package eu.algites.lib.data.smartdataobject.impl.standard;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies that the SmartDataObject metadata index is based on the getter's
 * canonical semantic identity, while preserving unique, externally visible
 * data property names and the inherited field's effective wire identity.
 */
public final class AItcSemanticFieldIdentityTest {
    /** Root contract whose data property deliberately differs from the getter identifier. */
    interface AIiCustomer {
        /** Obtain the customer identifier. @return customer identifier */
        @AIaDataObjectField(fieldName = "external_customer_id")
        String getCustomerId();
    }

    /** Restrict the inherited property without repeating its external name. */
    interface AIiCustomerChild extends AIiCustomer {
        /** Obtain the required identifier. @return customer identifier */
        @Override
        @AIaDataObjectField(presence = AInFieldPresenceKind.REQUIRED)
        String getCustomerId();
    }

    /** Invalid redeclaration of an inherited data name. */
    interface AIiInvalidOverride extends AIiCustomer {
        /** Obtain the conflicting identifier. @return customer identifier */
        @Override
        @AIaDataObjectField(fieldName = "different")
        String getCustomerId();
    }

    /** Invalid declaration of a distinct getter with a colliding data name. */
    interface AIiInvalidAlias extends AIiCustomer {
        /** Obtain another identifier. @return other identifier */
        @AIaDataObjectField(fieldName = "external_customer_id")
        String getOtherId();
    }

    /** Test that a derived getter retains the parent's external data name. */
    @Test
    public void AIcInheritedFieldKeepsExternalName() {
        var locFields = AIcSmartDataMetadata.forReadContract(AIiCustomerChild.class).names();
        Assert.assertEquals(locFields.size(), 1);
        Assert.assertEquals(locFields.get("CustomerId").wireName(), "external_customer_id");
    }

    /** Test that external data names are immutable along override edges. */
    @Test
    public void AIcCannotRedefineInheritedFieldName() {
        Assert.expectThrows(IllegalArgumentException.class,
                () -> AIcSmartDataMetadata.forReadContract(AIiInvalidOverride.class));
    }

    /** Test that two independent semantic fields may not share a data name. */
    @Test
    public void AIcRejectDuplicateWireName() {
        Assert.expectThrows(IllegalArgumentException.class,
                () -> AIcSmartDataMetadata.forReadContract(AIiInvalidAlias.class));
    }
}
