package eu.algites.lib.common.version.scheme.gradle;

import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Renders a portable version requirement as a Gradle rich version constraint. */
public final class AIcGradleVersionRequirementRenderer {
	private AIcGradleVersionRequirementRenderer() { /* utility class */ }

	@Nonnull
	public static AIrGradleVersionConstraint render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "Version requirement must not be null");
		if (aRequirement.exactVersionText() != null) {
			return new AIrGradleVersionConstraint(
					null,
					aRequirement.exactVersionText(),
					null,
					aRequirement.excludedVersionTexts()
			);
		}

		String locRange = renderRange(aRequirement.minimum(), aRequirement.maximum());
		String locRequire = null;
		String locStrictly = null;
		if (!locRange.isEmpty()) {
			if (aRequirement.maximum() != null && aRequirement.maximumStrict()) {
				locStrictly = locRange;
			} else {
				locRequire = locRange;
			}
		}

		return new AIrGradleVersionConstraint(
				locRequire,
				locStrictly,
				aRequirement.preferredVersionText(),
				aRequirement.excludedVersionTexts()
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
