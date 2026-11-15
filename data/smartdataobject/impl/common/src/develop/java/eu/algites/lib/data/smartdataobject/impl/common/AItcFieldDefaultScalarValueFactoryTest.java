package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.*;
import eu.algites.lib.factory.cpdiscovery.*;
import java.math.BigInteger;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Runtime resolution of independent default declarations through CPDiscovery. */
public class AItcFieldDefaultScalarValueFactoryTest {
    public static class Sample extends AIcSmartDataObject {
        @AIaDataObjectField(dataFieldName = "Number", presence = AInFieldPresenceKind.REQUIRED)
        @AIaDataObjectFieldDefault(kind = AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT,
                defaultValueFactory = @AIaCategoryProviderFactoryDefinition(
                        factoryInterface = AIiFieldDefaultScalarValueFactory.class),
                defaultArguments = {"12345678901234567890"})
        public BigInteger getNumber() { return (BigInteger) get_EffectiveField("Number"); }

        @AIaDataObjectField(dataFieldName = "Fixed")
        @AIaDataObjectFieldDefault(kind = AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT,
                defaultValueFactory = @AIaCategoryProviderFactoryDefinition(
                        factoryInterface = AIiFieldDefaultScalarValueFactory.class),
                defaultArguments = {"true"})
        public Boolean getFixed() { return (Boolean) get_EffectiveField("Fixed"); }
        public void setFixed(Boolean value) { set_RawField("Fixed", value); }
    }

    @Test
    public void scalarFactoryAndRawPresence() {
        Sample value = new Sample();
        Assert.assertEquals(value.getNumber(), new BigInteger("12345678901234567890"));
        Assert.assertFalse(value.isRawFieldPresent("Number"));
        Assert.assertEquals(value.get_RawNotPresentRequiredFields(), java.util.Set.of("Number"));
        Assert.assertEquals(value.get_EffectiveNotPresentRequiredFields(), java.util.Set.of());
        value.setFixed(true);
        Assert.expectThrows(IllegalArgumentException.class, () -> value.setFixed(false));
        Assert.expectThrows(IllegalArgumentException.class, () -> value.setFixed(null));
    }
}
