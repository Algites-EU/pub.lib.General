package eu.algites.lib.common.version.scheme.conversion;

import jakarta.annotation.Nonnull;

/** Generic explicit converter between two version-scheme representations. */
@FunctionalInterface
public interface AIiVersionConverter<S, T> {
	@Nonnull
	AIrVersionConversionResult<T> convert(@Nonnull S aSource);
}
