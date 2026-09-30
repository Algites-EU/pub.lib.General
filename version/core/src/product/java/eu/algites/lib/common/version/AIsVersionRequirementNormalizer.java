package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Normalizes effective version requirements and validates hard constraints against a concrete version scheme. */
public final class AIsVersionRequirementNormalizer {
	private AIsVersionRequirementNormalizer() {
		/* utility class */
	}

	@Nonnull
	public static AIrVersionRequirementNormalization normalize(
			@Nonnull final AIiVersionRequirement aRequirement,
			@Nonnull final AIiVersionScheme aScheme
	) {
		Objects.requireNonNull(aRequirement, "Version requirement must not be null");
		Objects.requireNonNull(aScheme, "Version scheme must not be null");

		List<String> locInformationMessages = new ArrayList<>();
		AIiVersionBound locMinimum = aRequirement.minimum();
		AIiVersionBound locMaximum = aRequirement.maximum();
		boolean locMaximumStrict = aRequirement.effectiveMaximumStrict();

		if (locMinimum != null && locMaximum != null) {
			int locRangeComparison = compare(locMinimum.versionText(), locMaximum.versionText(), aScheme);
			boolean locRangeEmpty = locRangeComparison > 0
					|| (locRangeComparison == 0 && (!locMinimum.inclusive() || !locMaximum.inclusive()));
			if (locRangeEmpty) {
				if (locMaximumStrict) {
					throw new IllegalArgumentException("Minimum and strict maximum version constraints do not overlap");
				}
				locInformationMessages.add(
						"Non-strict maximum version " + locMaximum.versionText()
								+ " is ignored because it does not overlap the minimum version constraint " + locMinimum.versionText()
				);
				locMaximum = null;
				locMaximumStrict = false;
			}
		}

		String locExact = aRequirement.exactVersionText();
		if (locExact != null) {
			validateExactAgainstHardConstraints(locExact, locMinimum, locMaximum, locMaximumStrict, aRequirement.excludedVersionTexts(), aScheme);
			if (locMaximum != null && !locMaximumStrict && !matchesMaximum(locExact, locMaximum, aScheme)) {
				locInformationMessages.add(
						"Non-strict maximum version " + locMaximum.versionText()
								+ " is ignored because exact version " + locExact + " is effective"
				);
			}
			String locPreferred = aRequirement.preferredVersionText();
			if (locPreferred != null && compare(locPreferred, locExact, aScheme) != 0) {
				locInformationMessages.add(
						"Preferred version " + locPreferred + " is ignored because exact version " + locExact + " is effective"
				);
			}
			return new AIrVersionRequirementNormalization(AIcVersionRequirement.exact(locExact), locInformationMessages);
		}

		String locPreferred = aRequirement.preferredVersionText();
		if (locPreferred != null && !matchesHardConstraints(
				locPreferred,
				locMinimum,
				locMaximum,
				locMaximumStrict,
				aRequirement.excludedVersionTexts(),
				aScheme
		)) {
			locInformationMessages.add(
					"Preferred version " + locPreferred + " is ignored because it does not satisfy the effective hard version constraints"
			);
			locPreferred = null;
		}

		AIcVersionRequirement locNormalizedRequirement = new AIcVersionRequirement(
				null,
				locMinimum,
				locMaximum,
				locMaximum == null ? null : locMaximumStrict,
				aRequirement.excludedVersionTexts(),
				locPreferred
		);
		return new AIrVersionRequirementNormalization(locNormalizedRequirement, locInformationMessages);
	}

	private static void validateExactAgainstHardConstraints(
			@Nonnull final String aExactVersion,
			final AIiVersionBound aMinimum,
			final AIiVersionBound aMaximum,
			final boolean aMaximumStrict,
			@Nonnull final List<String> aExcludedVersions,
			@Nonnull final AIiVersionScheme aScheme
	) {
		if (aMinimum != null && !matchesMinimum(aExactVersion, aMinimum, aScheme)) {
			throw new IllegalArgumentException(
					"Exact version " + aExactVersion + " does not satisfy minimum version constraint " + aMinimum.versionText()
			);
		}
		if (aMaximum != null && aMaximumStrict && !matchesMaximum(aExactVersion, aMaximum, aScheme)) {
			throw new IllegalArgumentException(
					"Exact version " + aExactVersion + " does not satisfy strict maximum version constraint " + aMaximum.versionText()
			);
		}
		for (String locExcluded : aExcludedVersions) {
			if (compare(aExactVersion, locExcluded, aScheme) == 0) {
				throw new IllegalArgumentException("Exact version " + aExactVersion + " is explicitly excluded");
			}
		}
	}

	private static boolean matchesHardConstraints(
			@Nonnull final String aVersion,
			final AIiVersionBound aMinimum,
			final AIiVersionBound aMaximum,
			final boolean aMaximumStrict,
			@Nonnull final List<String> aExcludedVersions,
			@Nonnull final AIiVersionScheme aScheme
	) {
		if (aMinimum != null && !matchesMinimum(aVersion, aMinimum, aScheme)) {
			return false;
		}
		if (aMaximum != null && aMaximumStrict && !matchesMaximum(aVersion, aMaximum, aScheme)) {
			return false;
		}
		for (String locExcluded : aExcludedVersions) {
			if (compare(aVersion, locExcluded, aScheme) == 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean matchesMinimum(
			@Nonnull final String aVersion,
			@Nonnull final AIiVersionBound aMinimum,
			@Nonnull final AIiVersionScheme aScheme
	) {
		int locComparison = compare(aVersion, aMinimum.versionText(), aScheme);
		return locComparison > 0 || (locComparison == 0 && aMinimum.inclusive());
	}

	private static boolean matchesMaximum(
			@Nonnull final String aVersion,
			@Nonnull final AIiVersionBound aMaximum,
			@Nonnull final AIiVersionScheme aScheme
	) {
		int locComparison = compare(aVersion, aMaximum.versionText(), aScheme);
		return locComparison < 0 || (locComparison == 0 && aMaximum.inclusive());
	}

	private static int compare(
			@Nonnull final String aLeft,
			@Nonnull final String aRight,
			@Nonnull final AIiVersionScheme aScheme
	) {
		AIcVersion locLeft = aScheme.versionCodec().parseVersion(aLeft, aScheme);
		AIcVersion locRight = aScheme.versionCodec().parseVersion(aRight, aScheme);
		return AIsVersionComparator.compare(locLeft, locRight, aScheme);
	}
}
