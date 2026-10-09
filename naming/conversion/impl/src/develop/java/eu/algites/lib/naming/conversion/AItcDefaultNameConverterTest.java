package eu.algites.lib.naming.conversion;

import eu.algites.lib.naming.convention.AIcdInputVersionPolicy;
import eu.algites.lib.naming.convention.AInNameConvention;
import eu.algites.lib.naming.convention.AInVersionSource;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests deterministic naming and independent version handling. */
public final class AItcDefaultNameConverterTest {
    /** Verifies deterministic convention conversion while keeping the canonical version outside ordinary name tokens. */
    @Test
    public void convertsExplicitConventionsAndKeepsVersionSeparate() {
        AIcDefaultNameConverter locConverter = new AIcDefaultNameConverter();
        Assert.assertEquals(locConverter.convert("some-kebab-schema-name", AInNameConvention.LOWER_KEBAB_CASE, AInNameConvention.UPPER_CAMEL_CASE), "SomeKebabSchemaName");
        AIcdParsedVersionedName locParsed = locConverter.parseVersionedName("some-kebab-schema-name_1", 1, new AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, "_", true, true));
        Assert.assertEquals(locParsed.logicalName(), "some-kebab-schema-name");
        Assert.assertEquals(locParsed.version(), Integer.valueOf(1));
    }

    /** Verifies that strict input-version handling rejects disagreement between filename and explicit metadata. */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsVersionMismatchInStrictProfile() {
        new AIcDefaultNameConverter().parseVersionedName("some-kebab-schema-name_2", 1, new AIcdInputVersionPolicy(AInVersionSource.EXPLICIT_THEN_FILE_NAME, "_", true, true));
    }
}
