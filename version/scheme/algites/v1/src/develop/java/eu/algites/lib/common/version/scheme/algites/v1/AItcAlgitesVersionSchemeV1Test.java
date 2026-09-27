package eu.algites.lib.common.version.scheme.algites.v1;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for Algites version scheme v1. */
public class AItcAlgitesVersionSchemeV1Test {
	@Test
	public void testSnapshotCarriesInstanceSequence() {
		AIrAlgitesVersionV1 locVersion = AIcAlgitesVersionSchemeV1.snapshot("1", 0, 20260927012345678L);
		Assert.assertEquals(locVersion.baseVersionText(), "1.0");
		Assert.assertEquals(locVersion.qualifierKind(), AInAlgitesVersionQualifierKindV1.SNAPSHOT);
	}
}
