package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AInVersionBuildFormatPolicy}
 * </p>
 * <p>
 * Description: Defines how the build-identification part should be formatted when serializing versions.
 * </p>
 * <p>
 * Note: Formatting policy does not necessarily affect comparison. Schemes may choose to ignore build
 * information for precedence even if it is formatted.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public enum AInVersionBuildFormatPolicy {

	/**
	 * Build identification is not included in the formatted version string.
	 */
	OMIT,

	/**
	 * Build identification is included in the formatted version string as a dedicated build section.
	 */
	EMIT,

	/**
	 * Build identification is mapped into the scheme's qualifier/prerelease section (if supported).
	 * <p>
	 * This can be useful for ecosystems that do not natively support a dedicated build metadata section.
	 * </p>
	 */
	MAP_TO_QUALIFIER
}
