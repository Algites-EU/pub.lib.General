package eu.algites.lib.common.version.scheme.algites.v1;

/** Parser and formatter for Algites version scheme v1. */
public final class AIcAlgitesVersionSchemeV1 {
	private AIcAlgitesVersionSchemeV1() { /* utility class */ }

	public static AIrAlgitesVersionV1 release(final String aReleaseLine, final int aRevision) {
		return new AIrAlgitesVersionV1(aReleaseLine, aRevision, AInAlgitesVersionQualifierKindV1.RELEASE, null);
	}

	public static AIrAlgitesVersionV1 snapshot(final String aReleaseLine, final int aRevision, final Long aSnapshotInstance) {
		return new AIrAlgitesVersionV1(aReleaseLine, aRevision, AInAlgitesVersionQualifierKindV1.SNAPSHOT, aSnapshotInstance);
	}

	public static AIrAlgitesVersionV1 releaseCandidate(final String aReleaseLine, final int aRevision, final long aSequence) {
		return new AIrAlgitesVersionV1(aReleaseLine, aRevision, AInAlgitesVersionQualifierKindV1.RC, aSequence);
	}
}
