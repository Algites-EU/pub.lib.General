package eu.algites.lib.common.version.scheme.conversion.algites2pep440.v1;

import eu.algites.lib.common.version.scheme.algites.v1.AIiAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
import eu.algites.lib.common.version.scheme.conversion.AIrVersionConversionResult;
import eu.algites.lib.common.version.scheme.conversion.AIiVersionConverter;
import eu.algites.lib.common.version.scheme.pep440.AIrPep440VersionRequirement;
import jakarta.annotation.Nonnull;

/** Converts Algites version scheme v1 into PEP 440 publication-version syntax. */
public final class AIcAlgitesToPep440VersionConverterV1 implements AIiVersionConverter<AIiAlgitesVersionV1, AIrPep440VersionRequirement> {
	@Override
	@Nonnull
	public AIrVersionConversionResult<AIrPep440VersionRequirement> convert(@Nonnull final AIiAlgitesVersionV1 aSource) {
		String locText = aSource.baseVersionText();
		if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.SNAPSHOT) {
			locText += ".dev" + (aSource.qualifierSequence() == null ? 0 : aSource.qualifierSequence());
		} else if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.RC) {
			locText += "rc" + (aSource.qualifierSequence() == null ? 0 : aSource.qualifierSequence());
		}
		return AIrVersionConversionResult.exact(new AIrPep440VersionRequirement(locText));
	}
}
