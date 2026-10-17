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

	/**
	 * Version layout without a separate build component.
	 */
	NO_BUILD("no-build", true, "", AInVersionBuildComparisonPolicy.IGNORE),

	/**
	 * Build metadata after a plus sign, excluded from ordering.
	 */
	BUILD_AFTER_PLUS_IGNORED("build-after-plus-ignored", true, "+", AInVersionBuildComparisonPolicy.IGNORE),

	/**
	 * Build metadata after a plus sign, included in ordering.
	 */
	BUILD_AFTER_PLUS_ORDERED("build-after-plus-ordered", true, "+", AInVersionBuildComparisonPolicy.TOKEN_COMPARE),

	/**
	 * Build component before the plus delimiter, excluded from ordering.
	 */
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

	/** {@inheritDoc} */
	@Override
	public String code() {
		return code;
	}

	/** {@inheritDoc} */
	@Override
	public boolean versionBeforeBuild() {
		return versionBeforeBuild;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String buildDelimiter() {
		return buildDelimiter;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public AInVersionBuildComparisonPolicy buildComparisonPolicy() {
		return buildComparisonPolicy;
	}
}
