package eu.algites.lib.common.version;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/** Tests for portable version requirements. */
public class AItcVersionRequirementTest {
	@Test
	public void testCompactBoundaryParserAcceptsBothOperatorDirections() {
		AIiVersionBound locMinimumPrefix = AIcVersionRequirementParser.parseMinimum(">=1.2.0");
		AIiVersionBound locMinimumSuffix = AIcVersionRequirementParser.parseMinimum("1.2.0<=");
		AIiVersionBound locMaximumPrefix = AIcVersionRequirementParser.parseMaximum("<2.0.0");
		AIiVersionBound locMaximumSuffix = AIcVersionRequirementParser.parseMaximum("2.0.0>");

		Assert.assertEquals(locMinimumPrefix.versionText(), "1.2.0");
		Assert.assertTrue(locMinimumPrefix.inclusive());
		Assert.assertEquals(locMinimumSuffix.versionText(), "1.2.0");
		Assert.assertTrue(locMinimumSuffix.inclusive());
		Assert.assertEquals(locMaximumPrefix.versionText(), "2.0.0");
		Assert.assertFalse(locMaximumPrefix.inclusive());
		Assert.assertEquals(locMaximumSuffix.versionText(), "2.0.0");
		Assert.assertFalse(locMaximumSuffix.inclusive());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testMinimumRejectsMaximumOperatorDirection() {
		AIcVersionRequirementParser.parseMinimum("<1.2.0");
	}

	@Test
	public void testMaximumStrictRemainsUnspecifiedWithoutMaximum() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"1.5.2",
				null,
				null,
				null,
				List.of(),
				null
		);

		Assert.assertNull(locRequirement.maximumStrict());
		Assert.assertFalse(locRequirement.effectiveMaximumStrict());
	}

	@Test
	public void testMaximumStrictExplicitTrueWithoutMaximumIsIneffective() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"1.5.2",
				null,
				null,
				true,
				List.of(),
				null
		);

		Assert.assertEquals(locRequirement.maximumStrict(), Boolean.TRUE);
		Assert.assertFalse(locRequirement.effectiveMaximumStrict());
	}

	@Test
	public void testMaximumStrictDefaultsToTrueWithMaximum() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				null,
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				null,
				List.of(),
				null
		);

		Assert.assertNull(locRequirement.maximumStrict());
		Assert.assertTrue(locRequirement.effectiveMaximumStrict());
	}

	@Test
	public void testMaximumStrictCanBeExplicitlyFalseWithMaximum() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				null,
				AIcVersionRequirementParser.parseMaximum("<2.0.0"),
				false,
				List.of(),
				null
		);

		Assert.assertEquals(locRequirement.maximumStrict(), Boolean.FALSE);
		Assert.assertFalse(locRequirement.effectiveMaximumStrict());
	}


	@Test
	public void testEffectiveExactMayCoexistWithInheritedPreference() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"5",
				null,
				null,
				null,
				List.of(),
				"6"
		);

		AIrVersionRequirementNormalization locNormalized = AIsVersionRequirementNormalizer.normalize(
				locRequirement,
				AInBuiltinVersionScheme.MAVEN_DEFAULT
		);

		Assert.assertEquals(locNormalized.requirement().exactVersionText(), "5");
		Assert.assertNull(locNormalized.requirement().preferredVersionText());
		Assert.assertEquals(locNormalized.informationMessages().size(), 1);
	}

	@Test
	public void testExactMustSatisfyStrictRange() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"5",
				AIcVersionRequirementParser.parseMinimum(">=4"),
				AIcVersionRequirementParser.parseMaximum("<=6"),
				true,
				List.of(),
				null
		);

		AIrVersionRequirementNormalization locNormalized = AIsVersionRequirementNormalizer.normalize(
				locRequirement,
				AInBuiltinVersionScheme.MAVEN_DEFAULT
		);

		Assert.assertEquals(locNormalized.requirement().exactVersionText(), "5");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testExactOutsideStrictMaximumFailsNormalization() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"7",
				null,
				AIcVersionRequirementParser.parseMaximum("<=6"),
				true,
				List.of(),
				null
		);

		AIsVersionRequirementNormalizer.normalize(locRequirement, AInBuiltinVersionScheme.MAVEN_DEFAULT);
	}

	@Test
	public void testExactMayOverrideNonStrictMaximum() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				"7",
				null,
				AIcVersionRequirementParser.parseMaximum("<=6"),
				false,
				List.of(),
				null
		);

		AIrVersionRequirementNormalization locNormalized = AIsVersionRequirementNormalizer.normalize(
				locRequirement,
				AInBuiltinVersionScheme.MAVEN_DEFAULT
		);

		Assert.assertEquals(locNormalized.requirement().exactVersionText(), "7");
		Assert.assertEquals(locNormalized.informationMessages().size(), 1);
	}

	@Test
	public void testPreferredExcludedByMergedRequirementIsIgnoredDuringNormalization() {
		AIcVersionRequirement locRequirement = new AIcVersionRequirement(
				null,
				null,
				null,
				null,
				List.of("5"),
				"5"
		);

		AIrVersionRequirementNormalization locNormalized = AIsVersionRequirementNormalizer.normalize(
				locRequirement,
				AInBuiltinVersionScheme.MAVEN_DEFAULT
		);

		Assert.assertNull(locNormalized.requirement().preferredVersionText());
		Assert.assertEquals(locNormalized.informationMessages().size(), 1);
	}
}
