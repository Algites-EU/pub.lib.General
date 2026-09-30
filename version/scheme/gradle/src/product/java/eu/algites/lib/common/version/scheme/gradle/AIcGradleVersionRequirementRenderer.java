package eu.algites.lib.common.version.scheme.gradle;

import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import eu.algites.lib.common.version.AIsVersionRequirementNormalizer;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Renders a portable version requirement as a Gradle rich version constraint. */
public final class AIcGradleVersionRequirementRenderer {
	private AIcGradleVersionRequirementRenderer() { /* utility class */ }

	@Nonnull
	public static AIrGradleVersionConstraint render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "Version requirement must not be null");
		AIiVersionRequirement locRequirement = AIsVersionRequirementNormalizer
				.normalize(aRequirement, AIcGradleVersionScheme.INSTANCE)
				.requirement();
		if (locRequirement.exactVersionText() != null) {
			return new AIrGradleVersionConstraint(
					null,
					locRequirement.exactVersionText(),
					null,
					locRequirement.excludedVersionTexts()
			);
		}

		String locRange = renderRange(locRequirement.minimum(), locRequirement.maximum());
		String locRequire = null;
		String locStrictly = null;
		if (!locRange.isEmpty()) {
			if (locRequirement.maximum() != null && locRequirement.effectiveMaximumStrict()) {
				locStrictly = locRange;
			} else {
				locRequire = locRange;
			}
		}

		return new AIrGradleVersionConstraint(
				locRequire,
				locStrictly,
				locRequirement.preferredVersionText(),
				locRequirement.excludedVersionTexts()
		);
	}

	@Nonnull
	private static String renderRange(final AIiVersionBound aMinimum, final AIiVersionBound aMaximum) {
		if (aMinimum == null && aMaximum == null) {
			return "";
		}
		String locLeft = aMinimum == null ? "(" : (aMinimum.inclusive() ? "[" : "(");
		String locRight = aMaximum == null ? ")" : (aMaximum.inclusive() ? "]" : ")");
		String locMinimum = aMinimum == null ? "" : aMinimum.versionText();
		String locMaximum = aMaximum == null ? "" : aMaximum.versionText();
		return locLeft + locMinimum + "," + locMaximum + locRight;
	}
}
