package eu.algites.lib.common.version.scheme.conversion.algites2pep440.v1;

import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIcVersionRequirementParser;
import eu.algites.lib.common.version.scheme.pep440.AInPythonBuildPhase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/** Tests Algites v1 portable-requirement conversion into PEP 440 phases. */
public class AItcAlgitesVersionRequirementToPep440RendererV1Test {
	@Test
	public void testReleaseRequirementConversion() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				false,
				List.of("1.4.0"),
				"1.5.2"
		);
		Map<AInPythonBuildPhase, String> locRendered = AIcAlgitesVersionRequirementToPep440RendererV1.render(locRequirement);
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.PREFERRED), "==1.5.2,>=1.2.0,<2.0.0,!=1.4.0");
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.NON_STRICT_MAXIMUMS), ">=1.2.0,<2.0.0,!=1.4.0");
		Assert.assertEquals(locRendered.get(AInPythonBuildPhase.STRICT_MAXIMUMS), ">=1.2.0,!=1.4.0");
	}
}
