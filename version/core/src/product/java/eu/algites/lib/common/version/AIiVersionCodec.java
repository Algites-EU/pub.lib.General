package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AIiVersionCodec}
 * </p>
 * <p>
 * Description: Parses and formats version strings according to a {@link AIiVersionScheme}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public interface AIiVersionCodec {

	/**
	 * Parses a version text into {@link AIcVersion}.
	 * <p>
	 * Parsing is intentionally tolerant - the scheme mainly influences subsequent comparison and formatting.
	 * </p>
	 *
	 * @param aVersionText version text
	 * @param aScheme scheme
	 * @return parsed version
	 */
	@Nonnull
	AIcVersion parseVersion(@Nonnull String aVersionText, @Nonnull AIiVersionScheme aScheme);

	/**
	 * Formats a version into a string according to the given scheme.
	 *
	 * @param aVersion version
	 * @param aScheme scheme
	 * @return formatted text
	 */
	@Nonnull
	String formatVersionText(@Nonnull AIcVersion aVersion, @Nonnull AIiVersionScheme aScheme);

	/**
	 * Convenience: parses and re-formats the given text according to the scheme.
	 *
	 * @param aVersionText version text
	 * @param aScheme scheme
	 * @return normalized version text
	 */
	@Nonnull
	default String normalizeVersionText(@Nonnull final String aVersionText, @Nonnull final AIiVersionScheme aScheme) {
		return formatVersionText(parseVersion(aVersionText, aScheme), aScheme);
	}
}
