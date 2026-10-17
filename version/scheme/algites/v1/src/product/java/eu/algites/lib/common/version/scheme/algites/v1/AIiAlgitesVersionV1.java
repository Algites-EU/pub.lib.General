package eu.algites.lib.common.version.scheme.algites.v1;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Public contract of an Algites version scheme v1 value. */
public interface AIiAlgitesVersionV1 {
	/**
	 * Releases line version from the corresponding registry.
	 * @return the result of the operation
	 */
	@Nonnull
	String releaseLineVersion();

	/**
	 * Returns the revision number within the release line.
	 * @return the result of the operation
	 */
	int revision();

	/**
	 * Returns the classification of the Algites version qualifier.
	 * @return the result of the operation
	 */
	@Nonnull
	AInAlgitesVersionQualifierKindV1 qualifierKind();

	/**
	 * Returns the sequence number of a snapshot or release candidate, when present.
	 * @return the result of the operation
	 */
	@Nullable
	Long qualifierSequence();

	/**
	 * Returns the base version text without prerelease or snapshot qualifiers.
	 * @return the result of the operation
	 */
	@Nonnull
	String baseVersionText();
}
