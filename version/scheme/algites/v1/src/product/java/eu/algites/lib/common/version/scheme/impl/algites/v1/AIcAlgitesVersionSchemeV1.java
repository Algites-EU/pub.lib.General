package eu.algites.lib.common.version.scheme.impl.algites.v1;


import eu.algites.lib.common.version.scheme.algites.v1.AIiAlgitesVersionV1;
import eu.algites.lib.common.version.scheme.algites.v1.AInAlgitesVersionQualifierKindV1;
/** Parser and formatter for Algites version scheme v1. */
public final class AIcAlgitesVersionSchemeV1 {
	private AIcAlgitesVersionSchemeV1() { /* utility class */ }

	/**
	 * Releases  from the corresponding registry.
	 * @param aReleaseLineVersion release-line identifier
	 * @param aRevision revision number within the release line
	 * @return the result of the operation
	 */
	public static AIiAlgitesVersionV1 release(final String aReleaseLineVersion, final int aRevision) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.RELEASE, null);
	}

	/**
	 * Creates a snapshot version with the supplied release line, revision and instance.
	 * @param aReleaseLineVersion release-line identifier
	 * @param aRevision revision number within the release line
	 * @param aSnapshotInstance snapshot instance sequence number
	 * @return the result of the operation
	 */
	public static AIiAlgitesVersionV1 snapshot(final String aReleaseLineVersion, final int aRevision, final Long aSnapshotInstance) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.SNAPSHOT, aSnapshotInstance);
	}

	/**
	 * Releases candidate from the corresponding registry.
	 * @param aReleaseLineVersion release-line identifier
	 * @param aRevision revision number within the release line
	 * @param aSequence release candidate sequence number
	 * @return the result of the operation
	 */
	public static AIiAlgitesVersionV1 releaseCandidate(final String aReleaseLineVersion, final int aRevision, final long aSequence) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.RC, aSequence);
	}
}
