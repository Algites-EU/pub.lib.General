package eu.algites.lib.common.version.scheme.conversion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Result of an explicit version-scheme conversion.
 * @param value converted value in the target representation
 * @param quality degree of fidelity of the conversion
 * @param diagnostics diagnostic messages describing adjustments or limitations
 */
public record AIrVersionConversionResult<T>(
		@Nullable T value,
		@Nonnull AInVersionConversionQuality quality,
		@Nonnull List<String> diagnostics
) {
	/**
	 * Creates a version conversion result with its value, quality and diagnostics.
	 * @param value value supplied to the operation
	 * @param quality quality supplied to the operation
	 * @param diagnostics diagnostics supplied to the operation
	 */
	public AIrVersionConversionResult {
		Objects.requireNonNull(quality, "quality must not be null");
		diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		if (quality == AInVersionConversionQuality.UNSUPPORTED && value != null) {
			throw new IllegalArgumentException("Unsupported conversion must not contain a value");
		}
	}

	/**
	 * Returns the exact version requirement, when specified.
	 * @param aValue value being processed
	 * @return the result of the operation
	 */
	public static <T> AIrVersionConversionResult<T> exact(final T aValue) {
		return new AIrVersionConversionResult<>(Objects.requireNonNull(aValue), AInVersionConversionQuality.EXACT, List.of());
	}

	/**
	 * Normalizes d to the canonical representation.
	 * @param aValue value being processed
	 * @param aDiagnostic diagnostic supplied to the operation
	 * @return the result of the operation
	 */
	public static <T> AIrVersionConversionResult<T> normalized(final T aValue, final String aDiagnostic) {
		return new AIrVersionConversionResult<>(Objects.requireNonNull(aValue), AInVersionConversionQuality.LOSSLESS_WITH_NORMALIZATION, List.of(aDiagnostic));
	}
}
