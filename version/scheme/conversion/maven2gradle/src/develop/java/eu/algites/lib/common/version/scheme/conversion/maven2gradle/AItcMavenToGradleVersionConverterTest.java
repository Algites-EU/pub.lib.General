package eu.algites.lib.common.version.scheme.conversion.maven2gradle;

import eu.algites.lib.common.version.scheme.conversion.AInVersionConversionQuality;
import eu.algites.lib.common.version.scheme.maven.AIrMavenVersionRequirement;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Maven-to-Gradle conversion. */
public class AItcMavenToGradleVersionConverterTest {
	@Test
	public void testNumericRangeIsLosslessNormalization() {
		var locResult = new AIcMavenToGradleVersionConverter().convert(new AIrMavenVersionRequirement("[1.2,2.0)"));
		Assert.assertEquals(locResult.quality(), AInVersionConversionQuality.LOSSLESS_WITH_NORMALIZATION);
		Assert.assertEquals(locResult.value().strictly(), "[1.2,2.0[");
	}
}
