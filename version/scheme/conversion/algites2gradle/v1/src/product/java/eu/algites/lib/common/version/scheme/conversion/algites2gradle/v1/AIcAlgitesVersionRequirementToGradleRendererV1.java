package eu.algites.lib.common.version.scheme.conversion.algites2gradle.v1;

import eu.algites.lib.common.version.AIcVersionBound;
import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import eu.algites.lib.common.version.scheme.algites.v1.AIcAlgitesVersionTextV1;
import eu.algites.lib.common.version.scheme.gradle.AIcGradleVersionRequirementRenderer;
import eu.algites.lib.common.version.scheme.gradle.AIrGradleVersionConstraint;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/** Converts a portable requirement whose values use Algites v1 into a Gradle rich version constraint. */
public final class AIcAlgitesVersionRequirementToGradleRendererV1 {
	private static final AIcAlgitesToGradleVersionConverterV1 VERSION_CONVERTER = new AIcAlgitesToGradleVersionConverterV1();

	private AIcAlgitesVersionRequirementToGradleRendererV1() { /* utility class */ }

	@Nonnull
	public static AIrGradleVersionConstraint render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "version requirement must not be null");
		return AIcGradleVersionRequirementRenderer.render(convertRequirement(aRequirement));
	}

	@Nonnull
	private static AIcVersionRequirement convertRequirement(@Nonnull final AIiVersionRequirement aRequirement) {
		return new AIcVersionRequirement(
				convertVersion(aRequirement.exactVersionText()),
				convertBound(aRequirement.minimum()),
				convertBound(aRequirement.maximum()),
				aRequirement.maximumStrict(),
				aRequirement.excludedVersionTexts().stream().map(AIcAlgitesVersionRequirementToGradleRendererV1::convertVersion).toList(),
				convertVersion(aRequirement.preferredVersionText())
		);
	}

	private static AIiVersionBound convertBound(final AIiVersionBound aBound) {
		return aBound == null ? null : new AIcVersionBound(convertVersion(aBound.versionText()), aBound.inclusive());
	}

	private static String convertVersion(final String aVersion) {
		if (aVersion == null) return null;
		var locResult = VERSION_CONVERTER.convert(AIcAlgitesVersionTextV1.parse(aVersion));
		if (locResult.value() == null) {
			throw new IllegalArgumentException("Algites version '" + aVersion + "' cannot be converted to Gradle");
		}
		AIrGradleVersionConstraint locConstraint = locResult.value();
		String locVersion = locConstraint.strictly() != null ? locConstraint.strictly() : locConstraint.require();
		if (locVersion == null || locVersion.isBlank()) {
			throw new IllegalArgumentException("Algites version '" + aVersion + "' did not produce an exact Gradle version");
		}
		return locVersion;
	}
}
