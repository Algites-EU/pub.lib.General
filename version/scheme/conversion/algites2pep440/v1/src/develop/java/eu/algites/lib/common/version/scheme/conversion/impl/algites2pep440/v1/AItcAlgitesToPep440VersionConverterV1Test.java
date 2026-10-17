package eu.algites.lib.common.version.scheme.conversion.impl.algites2pep440.v1;

import eu.algites.lib.common.version.scheme.impl.algites.v1.AIcAlgitesVersionSchemeV1;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Algites-v1 to PEP 440 conversion. */
public class AItcAlgitesToPep440VersionConverterV1Test {
	@Test
	public void testSnapshotUsesDevRelease() {
		var locResult = new AIcAlgitesToPep440VersionConverterV1().convert(AIcAlgitesVersionSchemeV1.snapshot("1", 0, 42L));
		Assert.assertEquals(locResult.value().text(), "1.0.dev42");
	}
}
