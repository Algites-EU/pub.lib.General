package eu.algites.lib.common.version;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Title: {@link AItcVersionCodecTest}
 * </p>
 * <p>
 * Description: Tests demonstrating {@link AIiVersionCodec} and {@link AIiVersionFormatter} usage.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public class AItcVersionCodecTest {

	@Test
	public void testNormalizeOmitsBuildWhenConfigured() {
		AIiVersionScheme locScheme = new AIcCustomVersionScheme(
				"custom-omit-build",
				new AIcMavenLikeVersionComparator(),
				new AIcCustomVersionStructure("custom-omit-build-structure", true,
				"+",
				AInVersionBuildComparisonPolicy.IGNORE),
				AInBuiltinVersionFormat.OMIT_BUILD,
				AIcDefaultVersionCodec.INSTANCE
		);

		String locNormalized = locScheme.versionCodec().normalizeVersionText("1.2.3+45", locScheme);

		Assert.assertEquals(locNormalized, "1.2.3", "Build part must be omitted when policy is OMIT");
	}

	@Test
	public void testSemverSchemeEmitsBuildAndKeepsText() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_DEFAULT;

		String locNormalized = locScheme.versionCodec().normalizeVersionText("1.2.3+45", locScheme);

		Assert.assertEquals(locNormalized, "1.2.3+45", "SemVer scheme must keep build metadata in formatted output");
	}

	@Test
	public void testNormalizeMapsBuildToQualifier() {
		AIiVersionScheme locScheme = new AIcCustomVersionScheme(
				"custom-map-build",
				new AIcSemverLikeVersionComparator(),
				new AIcCustomVersionStructure("custom-map-build-structure", true,
						"+",
						AInVersionBuildComparisonPolicy.IGNORE),
				AInBuiltinVersionFormat.MAP_BUILD_TO_QUALIFIER,
				AIcDefaultVersionCodec.INSTANCE
		);

		String locNormalizedPlain = locScheme.versionCodec().normalizeVersionText("1.2.3+7", locScheme);
		Assert.assertEquals(locNormalizedPlain, "1.2.3-build.7", "Build part must be mapped into qualifier section");

		String locNormalizedWithQualifier = locScheme.versionCodec().normalizeVersionText("1.2.3-rc1+7", locScheme);
		Assert.assertEquals(locNormalizedWithQualifier, "1.2.3-rc1.build.7", "Build part must be appended as additional qualifier token");
	}
}
