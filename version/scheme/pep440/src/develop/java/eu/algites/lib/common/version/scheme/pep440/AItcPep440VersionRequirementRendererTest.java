package eu.algites.lib.common.version.scheme.pep440;

import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIcVersionRequirementParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/** Tests for Python build-phase rendering of portable version requirements. */
public class AItcPep440VersionRequirementRendererTest {
	@Test
	public void testPreferredDeclaredAndRelaxedRepresentations() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				false,
				List.of("1.4.0", "1.6.0"),
				"1.5.2"
		);

		Map<AInPythonBuildPhase, String> locRendered = AIcPep440VersionRequirementRenderer.render(locRequirement);
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.PREFERRED), "==1.5.2,>=1.2.0,<2.0.0,!=1.4.0,!=1.6.0");
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.NON_STRICT_MAXIMUMS), ">=1.2.0,<2.0.0,!=1.4.0,!=1.6.0");
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.STRICT_MAXIMUMS), ">=1.2.0,!=1.4.0,!=1.6.0");
	}

	@Test
	public void testStrictMaximumDoesNotCreateNonStrictPhase() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				true,
				List.of(),
				null
		);

		Map<AInPythonBuildPhase, String> locRendered = AIcPep440VersionRequirementRenderer.render(locRequirement);
		Assert.assertFalse(locRendered.containsKey(AInPythonBuildPhase.PREFERRED));
		Assert.assertFalse(locRendered.containsKey(AInPythonBuildPhase.NON_STRICT_MAXIMUMS));
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.STRICT_MAXIMUMS), ">=1.2.0,<2.0.0");
	}
}
