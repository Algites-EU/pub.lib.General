package eu.algites.lib.common.version.scheme.gradle;

import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIcVersionRequirementParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Tests for Gradle rendering of portable version requirements. */
public class AItcGradleVersionRequirementRendererTest {
	@Test
	public void testNonStrictMaximumRendersAsRequire() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				false,
				List.of("1.4.0"),
				"1.5.2"
		);

		AIrGradleVersionConstraint locRendered = AIcGradleVersionRequirementRenderer.render(locRequirement);
		Assert.assertEquals(locRendered.require(), "[1.2.0,2.0.0)");
		Assert.assertNull(locRendered.strictly());
		Assert.assertEquals(locRendered.prefer(), "1.5.2");
		Assert.assertEquals(locRendered.reject(), List.of("1.4.0"));
	}

	@Test
	public void testStrictMaximumRendersAsStrictly() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				AIcVersionRequirementParser.parseMinimum(">=1.2.0"),
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				true,
				List.of(),
				null
		);

		AIrGradleVersionConstraint locRendered = AIcGradleVersionRequirementRenderer.render(locRequirement);
		Assert.assertNull(locRendered.require());
		Assert.assertEquals(locRendered.strictly(), "[1.2.0,2.0.0)");
	}
}
