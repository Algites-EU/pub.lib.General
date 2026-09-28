package eu.algites.lib.common.version.scheme.conversion.algites2pep440.v1;

import eu.algites.lib.common.version.AIcVersionBound;
import eu.algites.lib.common.version.AIcVersionRequirement;
import eu.algites.lib.common.version.AIiVersionBound;
import eu.algites.lib.common.version.AIiVersionRequirement;
import eu.algites.lib.common.version.scheme.algites.v1.AIcAlgitesVersionTextV1;
import eu.algites.lib.common.version.scheme.conversion.AIrVersionConversionResult;
import eu.algites.lib.common.version.scheme.pep440.AIcPep440VersionRequirementRenderer;
import eu.algites.lib.common.version.scheme.pep440.AInPythonBuildPhase;
import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.Objects;

/** Converts a portable requirement whose values use Algites v1 into PEP 440 build-phase constraints. */
public final class AIcAlgitesVersionRequirementToPep440RendererV1 {
	private static final AIcAlgitesToPep440VersionConverterV1 VERSION_CONVERTER = new AIcAlgitesToPep440VersionConverterV1();

	private AIcAlgitesVersionRequirementToPep440RendererV1() { /* utility class */ }

	@Nonnull
	public static Map<AInPythonBuildPhase, String> render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "version requirement must not be null");
		return AIcPep440VersionRequirementRenderer.render(convertRequirement(aRequirement));
	}

	@Nonnull
	private static AIcVersionRequirement convertRequirement(@Nonnull final AIiVersionRequirement aRequirement) {
		return new AIcVersionRequirement(
				convertVersion(aRequirement.exactVersionText()),
				convertBound(aRequirement.minimum()),
				convertBound(aRequirement.maximum()),
				aRequirement.maximumStrict(),
				aRequirement.excludedVersionTexts().stream().map(AIcAlgitesVersionRequirementToPep440RendererV1::convertVersion).toList(),
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
			throw new IllegalArgumentException("Algites version '" + aVersion + "' cannot be converted to PEP 440");
		}
		return locResult.value().text();
	}
}
