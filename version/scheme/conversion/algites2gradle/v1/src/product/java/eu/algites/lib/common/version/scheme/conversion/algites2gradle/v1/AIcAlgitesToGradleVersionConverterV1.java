package eu.algites.lib.common.version.scheme.conversion.algites2gradle.v1;

import eu.algites.lib.common.version.scheme.algites.v1.AIrAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
import eu.algites.lib.common.version.scheme.conversion.AIrVersionConversionResult;
import eu.algites.lib.common.version.scheme.conversion.AIiVersionConverter;
import eu.algites.lib.common.version.scheme.gradle.AIrGradleVersionConstraint;
import jakarta.annotation.Nonnull;

/** Converts Algites version scheme v1 directly into a Gradle requirement. */
public final class AIcAlgitesToGradleVersionConverterV1 implements AIiVersionConverter<AIrAlgitesVersionV1, AIrGradleVersionConstraint> {
	@Override
	@Nonnull
	public AIrVersionConversionResult<AIrGradleVersionConstraint> convert(@Nonnull final AIrAlgitesVersionV1 aSource) {
		String locText = aSource.baseVersionText();
		if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.SNAPSHOT) {
			locText += "-SNAPSHOT";
		} else if (aSource.qualifierKind() == AInAlgitesVersionQualifierKindV1.RC) {
			locText += "-rc" + (aSource.qualifierSequence() == null ? 0 : aSource.qualifierSequence());
		}
		return AIrVersionConversionResult.exact(AIrGradleVersionConstraint.require(locText));
	}
}
