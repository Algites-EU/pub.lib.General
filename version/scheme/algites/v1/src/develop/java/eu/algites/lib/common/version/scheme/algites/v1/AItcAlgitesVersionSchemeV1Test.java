package eu.algites.lib.common.version.scheme.algites.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Algites version scheme v1. */
public class AItcAlgitesVersionSchemeV1Test {
	@Test
	public void testSnapshotCarriesInstanceSequence() {
		AIiAlgitesVersionV1 locVersion = AIcAlgitesVersionSchemeV1.snapshot("1", 0, 20260927012345678L);
		Assert.assertEquals(locVersion.baseVersionText(), "1.0");
		Assert.assertEquals(locVersion.qualifierKind(), AInAlgitesVersionQualifierKindV1.SNAPSHOT);
	}
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testReleaseLineVersionRejectsNonNumericClassifierText() {
		AIcAlgitesVersionSchemeV1.release("prod-1.3", 2);
	}

	@Test
	public void testCanonicalTextRoundTrip() {
		AIiAlgitesVersionV1 locVersion = AIcAlgitesVersionTextV1.parse("1.3.2-SNAPSHOT");
		Assert.assertEquals(locVersion.releaseLineVersion(), "1.3");
		Assert.assertEquals(locVersion.revision(), 2);
		Assert.assertEquals(AIcAlgitesVersionTextV1.render(locVersion), "1.3.2-SNAPSHOT");
	}

}
