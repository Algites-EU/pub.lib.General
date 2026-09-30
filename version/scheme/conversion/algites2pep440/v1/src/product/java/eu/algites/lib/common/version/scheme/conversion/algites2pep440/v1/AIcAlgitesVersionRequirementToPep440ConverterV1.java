package eu.algites.lib.common.version.scheme.conversion.algites2pep440.v1;

import eu.algites.lib.common.version.AIcVersionBound;
import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import eu.algites.lib.common.version.AIsVersionComparator;
import eu.algites.lib.common.version.scheme.algites.v1.AIcAlgitesVersionTextV1;
import eu.algites.lib.common.version.scheme.algites.v1.AIiAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
import eu.algites.lib.common.version.scheme.pep440.AIcPep440VersionScheme;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Converts portable Algites v1 requirements into portable requirements containing PEP 440 version values. */
public final class AIcAlgitesVersionRequirementToPep440ConverterV1 {
	private static final AIcAlgitesToPep440VersionConverterV1 VERSION_CONVERTER = new AIcAlgitesToPep440VersionConverterV1();

	private AIcAlgitesVersionRequirementToPep440ConverterV1() { /* utility class */ }

	@Nonnull
	public static AIcVersionRequirement convert(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "version requirement must not be null");
		String locExactVersionText = aRequirement.exactVersionText();
		if (locExactVersionText != null) {
			AIiAlgitesVersionV1 locExactVersion = AIcAlgitesVersionTextV1.parse(locExactVersionText);
			if (locExactVersion.qualifierKind() == AInAlgitesVersionQualifierKindV1.SNAPSHOT
					&& locExactVersion.qualifierSequence() == null) {
				return convertSnapshotSeriesRequirement(aRequirement, locExactVersion);
			}
		}
		return convertOrdinaryRequirement(aRequirement);
	}

	@Nonnull
	private static AIcVersionRequirement convertSnapshotSeriesRequirement(
			@Nonnull final AIiVersionRequirement aRequirement,
			@Nonnull final AIiAlgitesVersionV1 aExactSnapshotVersion
	) {
		AIiVersionBound locSnapshotMinimum = new AIcVersionBound(convertVersion(aRequirement.exactVersionText()), true);
		AIiVersionBound locEffectiveMinimum = strongerMinimum(locSnapshotMinimum, convertBound(aRequirement.minimum()));

		AIiVersionBound locSnapshotMaximum = new AIcVersionBound(aExactSnapshotVersion.baseVersionText() + "a0", false);
		AIiVersionBound locDeclaredStrictMaximum = aRequirement.maximum() != null && aRequirement.effectiveMaximumStrict()
				? convertBound(aRequirement.maximum())
				: null;
		AIiVersionBound locEffectiveMaximum = strongerMaximum(locSnapshotMaximum, locDeclaredStrictMaximum);

		return new AIcVersionRequirement(
				null,
				locEffectiveMinimum,
				locEffectiveMaximum,
				true,
				aRequirement.excludedVersionTexts().stream().map(AIcAlgitesVersionRequirementToPep440ConverterV1::convertVersion).toList(),
				null
		);
	}

	@Nonnull
	private static AIcVersionRequirement convertOrdinaryRequirement(@Nonnull final AIiVersionRequirement aRequirement) {
		return new AIcVersionRequirement(
				convertVersion(aRequirement.exactVersionText()),
				convertBound(aRequirement.minimum()),
				convertBound(aRequirement.maximum()),
				aRequirement.maximumStrict(),
				aRequirement.excludedVersionTexts().stream().map(AIcAlgitesVersionRequirementToPep440ConverterV1::convertVersion).toList(),
				convertVersion(aRequirement.preferredVersionText())
		);
	}

	private static AIiVersionBound convertBound(final AIiVersionBound aBound) {
		return aBound == null ? null : new AIcVersionBound(convertVersion(aBound.versionText()), aBound.inclusive());
	}

	private static AIiVersionBound strongerMinimum(final AIiVersionBound aLeft, final AIiVersionBound aRight) {
		if (aRight == null) return aLeft;
		int locComparison = comparePep440(aLeft.versionText(), aRight.versionText());
		if (locComparison > 0) return aLeft;
		if (locComparison < 0) return aRight;
		return new AIcVersionBound(aLeft.versionText(), aLeft.inclusive() && aRight.inclusive());
	}

	private static AIiVersionBound strongerMaximum(final AIiVersionBound aLeft, final AIiVersionBound aRight) {
		if (aRight == null) return aLeft;
		int locComparison = comparePep440(aLeft.versionText(), aRight.versionText());
		if (locComparison < 0) return aLeft;
		if (locComparison > 0) return aRight;
		return new AIcVersionBound(aLeft.versionText(), aLeft.inclusive() && aRight.inclusive());
	}

	private static int comparePep440(@Nonnull final String aLeft, @Nonnull final String aRight) {
		var locScheme = AIcPep440VersionScheme.INSTANCE;
		return AIsVersionComparator.compare(
				locScheme.versionCodec().parseVersion(aLeft, locScheme),
				locScheme.versionCodec().parseVersion(aRight, locScheme),
				locScheme
		);
	}

	private static String convertVersion(final String aVersion) {
		if (aVersion == null) return null;
		var locResult = VERSION_CONVERTER.convert(AIcAlgitesVersionTextV1.parse(aVersion));
		if (locResult.value() == null) {
			throw new IllegalArgumentException("Algites version '" + aVersion + "' cannot be converted to PEP 440");
		}
		return locResult.value().text();
	}
}
