package eu.algites.lib.common.version.scheme.conversion.algites2maven.v1;

import eu.algites.lib.common.version.scheme.algites.v1.AIrAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
import eu.algites.lib.common.version.scheme.conversion.AIrVersionConversionResult;
import eu.algites.lib.common.version.scheme.conversion.AIiVersionConverter;
import eu.algites.lib.common.version.scheme.maven.AIrMavenVersionRequirement;
import jakarta.annotation.Nonnull;

/** Converts Algites version scheme v1 into Maven publication-version syntax. */
public final class AIcAlgitesToMavenVersionConverterV1 implements AIiVersionConverter<AIrAlgitesVersionV1, AIrMavenVersionRequirement> {
	@Override
	@Nonnull
	public AIrVersionConversionResult<AIrMavenVersionRequirement> convert(@Nonnull final AIrAlgitesVersionV1 aSource) {
		String locText = aSource.baseVersionText();
		if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.SNAPSHOT) {
			locText += "-SNAPSHOT";
		} else if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.RC) {
			locText += "-rc" + (aSource.qualifierSequence() == null ? 0 : aSource.qualifierSequence());
		}
		return AIrVersionConversionResult.exact(new AIrMavenVersionRequirement(locText));
	}
}
