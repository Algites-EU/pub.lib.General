package eu.algites.lib.common.version.scheme.conversion.algites2pep440.v1;

import eu.algites.lib.common.version.AIiVersionRequirement;
import eu.algites.lib.common.version.scheme.pep440.AIcPep440VersionRequirementRenderer;
import eu.algites.lib.common.version.scheme.pep440.AInPythonBuildPhase;
import jakarta.annotation.Nonnull;

import java.util.Map;
import java.util.Objects;

/** Converts a portable requirement whose values use Algites v1 into PEP 440 build-phase constraints. */
public final class AIcAlgitesVersionRequirementToPep440RendererV1 {
	private AIcAlgitesVersionRequirementToPep440RendererV1() { /* utility class */ }

	@Nonnull
	public static Map<AInPythonBuildPhase, String> render(@Nonnull final AIiVersionRequirement aRequirement) {
		Objects.requireNonNull(aRequirement, "version requirement must not be null");
		return AIcPep440VersionRequirementRenderer.render(
				AIcAlgitesVersionRequirementToPep440ConverterV1.convert(aRequirement)
		);
	}
}
