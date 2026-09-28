package eu.algites.lib.common.version.scheme.algites.v1;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Public contract of an Algites version scheme v1 value. */
public interface AIiAlgitesVersionV1 {
	@Nonnull
	String releaseLineVersion();

	int revision();

	@Nonnull
	AInAlgitesVersionQualifierKindV1 qualifierKind();

	@Nullable
	Long qualifierSequence();

	@Nonnull
	String baseVersionText();
}
