package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AInBuiltinVersionStructure}
 * </p>
 * <p>
 * Description: Built-in {@link AIiVersionStructure} definitions.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public enum AInBuiltinVersionStructure implements AIiVersionStructure {

	NO_BUILD("no-build", true, "", AInVersionBuildComparisonPolicy.IGNORE),

	BUILD_AFTER_PLUS_IGNORED("build-after-plus-ignored", true, "+", AInVersionBuildComparisonPolicy.IGNORE),

	BUILD_AFTER_PLUS_ORDERED("build-after-plus-ordered", true, "+", AInVersionBuildComparisonPolicy.TOKEN_COMPARE),

	BUILD_BEFORE_PLUS_IGNORED("build-before-plus-ignored", false, "+", AInVersionBuildComparisonPolicy.IGNORE),
  ;

	private final String code;
	private final boolean versionBeforeBuild;
	private final String buildDelimiter;
	private final AInVersionBuildComparisonPolicy buildComparisonPolicy;

	AInBuiltinVersionStructure(
			final String aCode,
			final boolean aVersionBeforeBuild,
			@Nonnull final String aBuildDelimiter,
			@Nonnull final AInVersionBuildComparisonPolicy aBuildComparisonPolicy
	) {
		code = aCode;
		versionBeforeBuild = aVersionBeforeBuild;
		buildDelimiter = aBuildDelimiter;
		buildComparisonPolicy = aBuildComparisonPolicy;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public boolean versionBeforeBuild() {
		return versionBeforeBuild;
	}

	@Override
	@Nonnull
	public String buildDelimiter() {
		return buildDelimiter;
	}

	@Override
	@Nonnull
	public AInVersionBuildComparisonPolicy buildComparisonPolicy() {
		return buildComparisonPolicy;
	}
}
