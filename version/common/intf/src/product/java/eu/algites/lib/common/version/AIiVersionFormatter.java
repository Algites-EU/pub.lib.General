package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AIiVersionFormatter}
 * </p>
 * <p>
 * Description: Formats {@link AIcVersion} objects to strings according to a {@link AIiVersionScheme}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
@FunctionalInterface
public interface AIiVersionFormatter {

	/**
	 * Formats a version as a string for the given scheme.
	 *
	 * @param aVersion version to format
	 * @param aScheme scheme providing structure and format rules
	 * @return formatted version text
	 */
	@Nonnull
	String formatVersionText(@Nonnull AIcVersion aVersion, @Nonnull AIiVersionScheme aScheme);
}
