package eu.algites.lib.naming.convention;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Verifies that output naming can be customized without mutating an existing profile. */
public class AItcNamingProfileDerivationTest {
    @Test
    public void derivedRulesDoNotChangeOriginal() {
        var locOriginal = new AIcdNamingProfile(
            java.util.Map.of(AInInputNameKind.DEFINITION, AInNameConvention.LOWER_KEBAB_CASE),
            java.util.Map.of(AInOutputNameKind.DATA_TYPE,
                new AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "cgd", "")),
            new AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, "_", true, true),
            new AIcdOutputVersionPolicy(true, "_", "", ""));
        var locReplacement = new AIcdOutputNameRule(AInNameConvention.UPPER_CAMEL_CASE, "AI", "cgsdo", "");
        var locDerived = locOriginal.withOutputRule(AInOutputNameKind.DATA_TYPE, locReplacement);
        Assert.assertEquals(locOriginal.outputRules().get(AInOutputNameKind.DATA_TYPE).typeMarker(), "cgd");
        Assert.assertEquals(locDerived.outputRules().get(AInOutputNameKind.DATA_TYPE).typeMarker(), "cgsdo");
        Assert.assertNotSame(locOriginal, locDerived);
        Assert.expectThrows(UnsupportedOperationException.class, () -> locDerived.outputRules().clear());
    }
}
