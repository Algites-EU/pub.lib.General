package eu.algites.lib.common.version.scheme.maven;

import eu.algites.lib.common.version.AIcVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AIcMavenVersionScheme}.
 */
public class AItcMavenVersionSchemeTest {

	@Test
	public void testSnapshotPrecedesRelease() {
		int locResult = AIcMavenVersionScheme.INSTANCE.versionComparator().compare(
				new AIcVersion("1.0-SNAPSHOT"),
				new AIcVersion("1.0")
		);
		Assert.assertTrue(locResult < 0);
	}
}
