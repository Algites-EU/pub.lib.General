package eu.algites.lib.common.version;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Title: {@link AItcVersionComparatorTest}
 * </p>
 * <p>
 * Description: TestNG tests for {@link AIsVersionTokenizer} and {@link AIsVersionComparator}.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public class AItcVersionComparatorTest {

	@Test
	public void testTokenizerSplitsAlphaNumericAndSeparators() {
		AIcVersion locVersion = new AIcVersion("1.0-rc1+build.7");

		Assert.assertTrue(locVersion.getTokens().size() >= 3, "Token count must be non-trivial");
		Assert.assertEquals(locVersion.getTokens().get(0).getTokenType(), AInVersionTokenType.ALPHANUMERIC, "First token must be alphanumeric");
		Assert.assertEquals(locVersion.getTokens().get(0).getText(), "1", "First alphanumeric token must be '1'");
	}

	@Test
	public void testMavenLikeReleaseBeatsRc() {
		AIcVersion locRelease = new AIcVersion("1.0");
		AIcVersion locRc = new AIcVersion("1.0-rc1");

		int locCmp = AIsVersionComparator.compare(locRelease, locRc, AInBuiltinVersionScheme.MAVEN_DEFAULT);
		Assert.assertTrue(locCmp > 0, "Release must be greater than RC");
	}

	@Test
	public void testMavenLikeSnapshotIsLower() {
		AIcVersion locSnapshot = new AIcVersion("1.0-SNAPSHOT");
		AIcVersion locRelease = new AIcVersion("1.0");

		int locCmp = AIsVersionComparator.compare(locSnapshot, locRelease, AInBuiltinVersionScheme.MAVEN_DEFAULT);
		Assert.assertTrue(locCmp < 0, "Snapshot must be lower than release");
	}

	@Test
	public void testMavenLikeNumericComparesNumerically() {
		AIcVersion locA = new AIcVersion("1.10");
		AIcVersion locB = new AIcVersion("1.2");

		int locCmp = AIsVersionComparator.compare(locA, locB, AInBuiltinVersionScheme.MAVEN_DEFAULT);
		Assert.assertTrue(locCmp > 0, "1.10 must be greater than 1.2");
	}

	@Test
	public void testSemVerIgnoresBuildMetadataForPrecedence() {
		AIcVersion locA = new AIcVersion("1.2.3+build.7");
		AIcVersion locB = new AIcVersion("1.2.3+build.8");

		int locCmp = AIsVersionComparator.compare(locA, locB, AInBuiltinVersionScheme.SEMVER_DEFAULT);
		Assert.assertEquals(locCmp, 0, "Build metadata must be ignored for precedence");
	}

	@Test
	public void testCompareDelegatesToComparatorViaMode() {
		AIcVersion locA = new AIcVersion("1.0");
		AIcVersion locB = new AIcVersion("2.0");

		AIiVersionComparator locComparator = (aLeft, aRight) -> 123;
		AIiVersionScheme locMode = new AIcCustomVersionScheme("test", locComparator,
				AInBuiltinVersionStructure.NO_BUILD,
				AInBuiltinVersionFormat.OMIT_BUILD);

		int locCmp = AIsVersionComparator.compare(locA, locB, locMode);
		Assert.assertEquals(locCmp, 123, "compare(left,right,purpose) must delegate to purpose.versionComparator()");
	}

	@Test
	public void testCompareDelegatesToProvidedComparator() {
		AIcVersion locA = new AIcVersion("1.0");
		AIcVersion locB = new AIcVersion("2.0");

		AIiVersionComparator locComparator = (aLeft, aRight) -> -456;

		int locCmp = AIsVersionComparator.compare(locA, locB, locComparator);
		Assert.assertEquals(locCmp, -456, "compare(left,right,comparator) must delegate to comparator");
	}

	@Test
	public void testAIcVersionCompareToDelegatesToHandlingModeComparator() {
		AIcVersion locA = new AIcVersion("1.0");
		AIcVersion locB = new AIcVersion("2.0");

		AIiVersionComparator locComparator = (aLeft, aRight) -> 77;
		AIiVersionScheme locMode = new AIcCustomVersionScheme("test2", locComparator,
				AInBuiltinVersionStructure.NO_BUILD,
				AInBuiltinVersionFormat.OMIT_BUILD);

		int locCmp = locA.compareTo(locB, locMode);
		Assert.assertEquals(locCmp, 77, "AIcVersion.compareTo(other, purpose) must delegate to purpose.versionComparator()");
	}
	@Test
	public void testMavenBuildMetadataIgnoredSchemeIgnoresBuild() {
		AIcVersion locA = new AIcVersion("1.2.3+build.7");
		AIcVersion locB = new AIcVersion("1.2.3+build.8");

		int locCmp = AIsVersionComparator.compare(locA, locB, AInBuiltinVersionScheme.MAVEN_BUILD_METADATA_IGNORED);
		Assert.assertEquals(locCmp, 0, "Maven build-metadata ignored scheme must ignore build section for precedence");
	}

	@Test
	public void testSemVerBuildOrderedSchemeComparesBuildIfEnabled() {
		AIcVersion locA = new AIcVersion("1.2.3+build.7");
		AIcVersion locB = new AIcVersion("1.2.3+build.8");

		int locCmp = AIsVersionComparator.compare(locA, locB, AInBuiltinVersionScheme.SEMVER_BUILD_ORDERED);
		Assert.assertTrue(locCmp < 0, "SemVer build-ordered scheme must compare build section when base version is equal");
	}

	@Test
	public void testSemVerBuildFirstSchemeSplitsBuildBeforeVersion() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_BUILD_FIRST;

		AIiVersionSchemeTextParts locParts = locScheme.versionStructure().splitVersionAndBuildText("build.7+1.2.3");
		Assert.assertEquals(locParts.versionText(), "1.2.3", "Split must treat the part after delimiter as version when versionBeforeBuild is false");
		Assert.assertEquals(locParts.buildText(), "build.7", "Split must treat the part before delimiter as build when versionBeforeBuild is false");

		AIcVersion locA = new AIcVersion("build.7+1.2.3");
		AIcVersion locB = new AIcVersion("build.8+1.2.3");

		int locCmp = AIsVersionComparator.compare(locA, locB, locScheme);
		Assert.assertEquals(locCmp, 0, "Build-first scheme ignoring build comparison must ignore build differences when base versions are equal");
	}

@Test
public void testBuiltinSchemeReferencesExpectedStructure() {
	Assert.assertSame(
			AInBuiltinVersionScheme.MAVEN_DEFAULT.versionStructure(),
			AInBuiltinVersionStructure.NO_BUILD,
			"MAVEN scheme must use NO_BUILD structure"
	);

	Assert.assertSame(
			AInBuiltinVersionScheme.SEMVER_DEFAULT.versionStructure(),
			AInBuiltinVersionStructure.BUILD_AFTER_PLUS_IGNORED,
			"SEMVER scheme must use BUILD_AFTER_PLUS_IGNORED structure"
	);

	Assert.assertSame(
			AInBuiltinVersionScheme.SEMVER_BUILD_ORDERED.versionStructure(),
			AInBuiltinVersionStructure.BUILD_AFTER_PLUS_ORDERED,
			"SEMVER_BUILD_ORDERED scheme must use BUILD_AFTER_PLUS_ORDERED structure"
	);

	Assert.assertSame(
			AInBuiltinVersionScheme.SEMVER_BUILD_FIRST.versionStructure(),
			AInBuiltinVersionStructure.BUILD_BEFORE_PLUS_IGNORED,
			"SEMVER_BUILD_FIRST scheme must use BUILD_BEFORE_PLUS_IGNORED structure"
	);
}

}
