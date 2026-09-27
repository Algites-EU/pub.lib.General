package eu.algites.lib.common.version.scheme.conversion.algites2gradle.v1;

import eu.algites.lib.common.version.scheme.algites.v1.AIcAlgitesVersionSchemeV1;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Algites-v1 to Gradle conversion. */
public class AItcAlgitesToGradleVersionConverterV1Test {
	@Test
	public void testReleaseConversion() {
		var locResult = new AIcAlgitesToGradleVersionConverterV1().convert(AIcAlgitesVersionSchemeV1.release("2", 3));
		Assert.assertEquals(locResult.value().require(), "2.3");
	}
}
