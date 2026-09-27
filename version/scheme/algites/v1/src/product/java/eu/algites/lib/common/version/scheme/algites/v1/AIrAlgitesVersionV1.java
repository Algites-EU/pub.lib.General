package eu.algites.lib.common.version.scheme.algites.v1;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;

/** Structured value of Algites version scheme v1. */
public record AIrAlgitesVersionV1(
		@Nonnull String releaseLine,
		int revision,
		@Nonnull AInAlgitesVersionQualifierKindV1 qualifierKind,
		@Nullable Long qualifierSequence
) {
	public AIrAlgitesVersionV1 {
		Objects.requireNonNull(releaseLine, "releaseLine must not be null");
		Objects.requireNonNull(qualifierKind, "qualifierKind must not be null");
		if (releaseLine.isBlank()) throw new IllegalArgumentException("releaseLine must not be blank");
		if (revision < 0) throw new IllegalArgumentException("revision must not be negative");
		if (qualifierSequence != null && qualifierSequence < 0) throw new IllegalArgumentException("qualifierSequence must not be negative");
	}

	@Nonnull
	public String baseVersionText() {
		return releaseLine + "." + revision;
	}
}
