package eu.algites.lib.common.version.scheme.algites.v1;

/** Parser and formatter for Algites version scheme v1. */
public final class AIcAlgitesVersionSchemeV1 {
	private AIcAlgitesVersionSchemeV1() { /* utility class */ }

	public static AIiAlgitesVersionV1 release(final String aReleaseLineVersion, final int aRevision) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.RELEASE, null);
	}

	public static AIiAlgitesVersionV1 snapshot(final String aReleaseLineVersion, final int aRevision, final Long aSnapshotInstance) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.SNAPSHOT, aSnapshotInstance);
	}

	public static AIiAlgitesVersionV1 releaseCandidate(final String aReleaseLineVersion, final int aRevision, final long aSequence) {
		return new AIcAlgitesVersionV1(aReleaseLineVersion, aRevision, AInAlgitesVersionQualifierKindV1.RC, aSequence);
	}
}
