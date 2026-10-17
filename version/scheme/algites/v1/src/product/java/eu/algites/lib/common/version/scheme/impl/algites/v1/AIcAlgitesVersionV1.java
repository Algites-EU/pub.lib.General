package eu.algites.lib.common.version.scheme.impl.algites.v1;


import eu.algites.lib.common.version.scheme.algites.v1.AIiAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/** Default immutable implementation of {@link AIiAlgitesVersionV1}. */
public final class AIcAlgitesVersionV1 implements AIiAlgitesVersionV1 {
	@Nonnull
	private final String releaseLineVersion;
	private final int revision;
	@Nonnull
	private final AInAlgitesVersionQualifierKindV1 qualifierKind;
	@Nullable
	private final Long qualifierSequence;

	/**
	 * Creates a algites version v1 instance with the supplied configuration.
	 * @param aReleaseLineVersion release-line identifier
	 * @param aRevision revision number within the release line
	 * @param aQualifierKind qualifier kind supplied to the operation
	 * @param aQualifierSequence qualifier sequence supplied to the operation
	 */
	public AIcAlgitesVersionV1(
			@Nonnull final String aReleaseLineVersion,
			final int aRevision,
			@Nonnull final AInAlgitesVersionQualifierKindV1 aQualifierKind,
			@Nullable final Long aQualifierSequence
	) {
		releaseLineVersion = Objects.requireNonNull(aReleaseLineVersion, "releaseLineVersion must not be null").trim();
		qualifierKind = Objects.requireNonNull(aQualifierKind, "qualifierKind must not be null");
		revision = aRevision;
		qualifierSequence = aQualifierSequence;
		if (releaseLineVersion.isEmpty()) {
			throw new IllegalArgumentException("releaseLineVersion must not be blank");
		}
		if (!releaseLineVersion.matches("[0-9]+(?:\\.[0-9]+)*")) {
			throw new IllegalArgumentException("releaseLineVersion must contain only numeric components separated by dots");
		}
		if (revision < 0) {
			throw new IllegalArgumentException("revision must not be negative");
		}
		if (qualifierSequence != null && qualifierSequence < 0) {
			throw new IllegalArgumentException("qualifierSequence must not be negative");
		}
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String releaseLineVersion() {
		return releaseLineVersion;
	}

	/** {@inheritDoc} */
	@Override
	public int revision() {
		return revision;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public AInAlgitesVersionQualifierKindV1 qualifierKind() {
		return qualifierKind;
	}

	/** {@inheritDoc} */
	@Override
	@Nullable
	public Long qualifierSequence() {
		return qualifierSequence;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String baseVersionText() {
		return releaseLineVersion + "." + revision;
	}
}
