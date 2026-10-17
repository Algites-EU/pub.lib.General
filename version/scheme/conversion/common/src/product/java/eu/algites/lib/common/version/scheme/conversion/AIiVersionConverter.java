package eu.algites.lib.common.version.scheme.conversion;

import jakarta.annotation.Nonnull;

/** Generic explicit converter between two version-scheme representations. */
@FunctionalInterface
public interface AIiVersionConverter<S, T> {
	/**
	 * Converts  to the target representation.
	 * @param aSource source value to convert into another representation
	 * @return the result of the operation
	 */
	@Nonnull
	AIrVersionConversionResult<T> convert(@Nonnull S aSource);
}
