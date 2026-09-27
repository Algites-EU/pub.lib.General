package eu.algites.lib.common.version.scheme.conversion.algites2maven.v1;

import eu.algites.lib.common.version.scheme.algites.v1.AIcAlgitesVersionSchemeV1;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Algites-v1 to Maven conversion. */
public class AItcAlgitesToMavenVersionConverterV1Test {
	@Test
	public void testSnapshotConversion() {
		var locResult = new AIcAlgitesToMavenVersionConverterV1().convert(AIcAlgitesVersionSchemeV1.snapshot("1", 0, 123L));
		Assert.assertEquals(locResult.value().text(), "1.0-SNAPSHOT");
	}
}
