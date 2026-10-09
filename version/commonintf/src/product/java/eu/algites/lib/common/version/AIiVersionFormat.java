package eu.algites.lib.common.version;

import eu.algites.lib.common.enums.AIiEnumItem;
import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AIiVersionFormat}
 * </p>
 * <p>
 * Description: Formatting preferences for rendering version texts under a given {@link AIiVersionScheme}.
 * </p>
 * <p>
 * Format specification is intentionally separate from {@link AIiVersionStructure}. The structure defines how
 * version text is split into precedence-relevant and build-identification parts, while the format specification
 * defines how such parts are rendered.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public interface AIiVersionFormat extends AIiEnumItem {

	/**
	 * @return policy defining how the build-identification part is formatted when serializing versions
	 */
	@Nonnull
	default AInVersionBuildFormatPolicy buildFormatPolicy() {
		return AInVersionBuildFormatPolicy.OMIT;
	}

	/**
	 * Prefix used when {@link #buildFormatPolicy()} is {@link AInVersionBuildFormatPolicy#MAP_TO_QUALIFIER}.
	 *
	 * @return build prefix token
	 */
	@Nonnull
	default String mappedBuildPrefix() {
		return "build";
	}

	/**
	 * Delimiter used to start a qualifier/prerelease section in the version part (e.g. {@code -} in SemVer).
	 *
	 * @return qualifier delimiter
	 */
	@Nonnull
	default String qualifierDelimiter() {
		return "-";
	}

	/**
	 * Token delimiter used inside the qualifier/prerelease section (e.g. {@code .} in SemVer).
	 *
	 * @return qualifier token delimiter
	 */
	@Nonnull
	default String qualifierTokenDelimiter() {
		return ".";
	}


}
