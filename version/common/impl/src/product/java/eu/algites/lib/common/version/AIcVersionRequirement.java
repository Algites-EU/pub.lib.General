package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/** Default immutable implementation of {@link AIiVersionRequirement}. */
public final class AIcVersionRequirement implements AIiVersionRequirement {
	@Nullable
	private final String exactVersionText;
	@Nullable
	private final AIiVersionBound minimum;
	@Nullable
	private final AIiVersionBound maximum;
	@Nullable
	private final Boolean maximumStrict;
	@Nonnull
	private final List<String> excludedVersionTexts;
	@Nullable
	private final String preferredVersionText;

	public AIcVersionRequirement(
			@Nullable final String aExactVersionText,
			@Nullable final AIiVersionBound aMinimum,
			@Nullable final AIiVersionBound aMaximum,
			@Nullable final Boolean aMaximumStrict,
			@Nullable final List<String> aExcludedVersionTexts,
			@Nullable final String aPreferredVersionText
	) {
		exactVersionText = normalizeNullable(aExactVersionText, "Exact version");
		minimum = aMinimum;
		maximum = aMaximum;
		maximumStrict = aMaximumStrict;
		excludedVersionTexts = normalizeVersions(aExcludedVersionTexts);
		preferredVersionText = normalizeNullable(aPreferredVersionText, "Preferred version");
	}

	public static AIcVersionRequirement exact(@Nonnull final String aVersionText) {
		return new AIcVersionRequirement(aVersionText, null, null, null, List.of(), null);
	}

	@Override
	@Nullable
	public String exactVersionText() {
		return exactVersionText;
	}

	@Override
	@Nullable
	public AIiVersionBound minimum() {
		return minimum;
	}

	@Override
	@Nullable
	public AIiVersionBound maximum() {
		return maximum;
	}

	@Override
	@Nullable
	public Boolean maximumStrict() {
		return maximumStrict;
	}

	@Override
	@Nonnull
	public List<String> excludedVersionTexts() {
		return excludedVersionTexts;
	}

	@Override
	@Nullable
	public String preferredVersionText() {
		return preferredVersionText;
	}

	@Nullable
	private static String normalizeNullable(@Nullable final String aValue, @Nonnull final String aLabel) {
		if (aValue == null) {
			return null;
		}
		String locValue = aValue.trim();
		if (locValue.isEmpty()) {
			throw new IllegalArgumentException(aLabel + " must not be blank");
		}
		return locValue;
	}

	@Nonnull
	private static List<String> normalizeVersions(@Nullable final List<String> aVersions) {
		if (aVersions == null || aVersions.isEmpty()) {
			return List.of();
		}
		return aVersions.stream()
				.map(aValue -> Objects.requireNonNull(aValue, "Excluded version must not be null").trim())
				.peek(aValue -> {
					if (aValue.isEmpty()) {
						throw new IllegalArgumentException("Excluded version must not be blank");
					}
				})
				.distinct()
				.toList();
	}
}
