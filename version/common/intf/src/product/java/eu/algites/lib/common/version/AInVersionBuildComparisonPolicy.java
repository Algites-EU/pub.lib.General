package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AInVersionBuildComparisonPolicy}
 * </p>
 * <p>
 * Description: Defines how the build-identification part of a version participates in comparison.
 * </p>
 * <p>
 * This is intentionally separated from the main version comparison because different version schemes
 * treat build-related information differently. For example, SemVer ignores build metadata for precedence.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public enum AInVersionBuildComparisonPolicy {

	/**
	 * Build identification is ignored during comparison.
	 */
	IGNORE,

	/**
	 * Build identification is compared when the "real" version parts are equal.
	 * <p>
	 * The comparison is performed token-wise with numeric-aware ordering.
	 * </p>
	 */
	TOKEN_COMPARE
}
