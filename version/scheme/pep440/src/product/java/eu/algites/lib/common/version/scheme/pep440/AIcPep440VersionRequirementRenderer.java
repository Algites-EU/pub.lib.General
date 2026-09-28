package eu.algites.lib.common.version.scheme.pep440;

import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Renders portable version requirements into PEP 440 constraints for Python build phases. */
public final class AIcPep440VersionRequirementRenderer {
	private AIcPep440VersionRequirementRenderer() { /* utility class */ }

	@Nonnull
	public static Map<AInPythonBuildPhase, String> render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "Version requirement must not be null");
		EnumMap<AInPythonBuildPhase, String> locResult = new EnumMap<>(AInPythonBuildPhase.class);

		if (aRequirement.exactVersionText() != null) {
			locResult.put(AInPythonBuildPhase.STRICT_MAXIMUMS, "==" + aRequirement.exactVersionText());
			return Map.copyOf(locResult);
		}

		if (aRequirement.preferredVersionText() != null) {
			String locDeclaredConstraint = renderConstraint(aRequirement, true);
			String locPreferredConstraint = "==" + aRequirement.preferredVersionText();
			locResult.put(
					AInPythonBuildPhase.PREFERRED,
					locDeclaredConstraint.isEmpty() ? locPreferredConstraint : locPreferredConstraint + "," + locDeclaredConstraint
			);
		}

		if (aRequirement.maximum() != null && !aRequirement.maximumStrict()) {
			locResult.put(AInPythonBuildPhase.NON_STRICT_MAXIMUMS, renderConstraint(aRequirement, true));
		}

		locResult.put(AInPythonBuildPhase.STRICT_MAXIMUMS, renderConstraint(aRequirement, aRequirement.maximumStrict()));
		return Map.copyOf(locResult);
	}

	@Nonnull
	private static String renderConstraint(@Nonnull final AIiVersionRequirement aRequirement, final boolean aIncludeMaximum) {
		List<String> locParts = new ArrayList<>();
		AIiVersionBound locMinimum = aRequirement.minimum();
		if (locMinimum != null) {
			locParts.add((locMinimum.inclusive() ? ">=" : ">") + locMinimum.versionText());
		}
		AIiVersionBound locMaximum = aRequirement.maximum();
		if (aIncludeMaximum && locMaximum != null) {
			locParts.add((locMaximum.inclusive() ? "<=" : "<") + locMaximum.versionText());
		}
		for (String locExcluded : aRequirement.excludedVersionTexts()) {
			locParts.add("!=" + locExcluded);
		}
		return String.join(",", locParts);
	}
}
