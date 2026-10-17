package eu.algites.lib.common.version.scheme.pep440;


import eu.algites.lib.common.version.scheme.impl.pep440.AIcPep440VersionScheme;
import eu.algites.lib.common.version.AIcVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for PEP 440 ordering. */
public class AItcPep440VersionSchemeTest {
	@Test
	public void testDevelopmentPrecedesPreReleaseAndRelease() {
		Assert.assertTrue(AIcPep440VersionScheme.INSTANCE.versionComparator().compare(new AIcVersion("1.0.dev1"), new AIcVersion("1.0a1")) < 0);
		Assert.assertTrue(AIcPep440VersionScheme.INSTANCE.versionComparator().compare(new AIcVersion("1.0rc1"), new AIcVersion("1.0")) < 0);
	}
}
