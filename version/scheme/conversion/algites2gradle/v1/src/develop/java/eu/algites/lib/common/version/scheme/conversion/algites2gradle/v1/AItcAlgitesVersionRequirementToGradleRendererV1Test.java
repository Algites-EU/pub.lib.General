package eu.algites.lib.common.version.scheme.conversion.algites2gradle.v1;

import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIcVersionRequirementParser;
import eu.algites.lib.common.version.scheme.gradle.AIrGradleVersionConstraint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Tests Algites v1 portable-requirement conversion into Gradle rich constraints. */
public class AItcAlgitesVersionRequirementToGradleRendererV1Test {
	@Test
	public void testReleaseRequirementConversion() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				true,
				List.of("1.4.0"),
				"1.5.2"
		);
		AIrGradleVersionConstraint locRendered = AIcAlgitesVersionRequirementToGradleRendererV1.render(locRequirement);
		Assert.assertEquals(locRendered.strictly(), "[1.2.0,2.0.0)");
		Assert.assertEquals(locRendered.prefer(), "1.5.2");
		Assert.assertEquals(locRendered.reject(), List.of("1.4.0"));
	}
}
