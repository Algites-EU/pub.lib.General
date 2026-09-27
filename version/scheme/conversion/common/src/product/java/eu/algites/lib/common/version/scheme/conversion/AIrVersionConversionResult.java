package eu.algites.lib.common.version.scheme.conversion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/** Result of an explicit version-scheme conversion. */
public record AIrVersionConversionResult<T>(
		@Nullable T value,
		@Nonnull AInVersionConversionQuality quality,
		@Nonnull List<String> diagnostics
) {
	public AIrVersionConversionResult {
		Objects.requireNonNull(quality, "quality must not be null");
		diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		if (quality == AInVersionConversionQuality.UNSUPPORTED && value != null) {
			throw new IllegalArgumentException("Unsupported conversion must not contain a value");
		}
	}

	public static <T> AIrVersionConversionResult<T> exact(final T aValue) {
		return new AIrVersionConversionResult<>(Objects.requireNonNull(aValue), AInVersionConversionQuality.EXACT, List.of());
	}

	public static <T> AIrVersionConversionResult<T> normalized(final T aValue, final String aDiagnostic) {
		return new AIrVersionConversionResult<>(Objects.requireNonNull(aValue), AInVersionConversionQuality.LOSSLESS_WITH_NORMALIZATION, List.of(aDiagnostic));
	}
}
