package eu.algites.lib.common.version.scheme.algites.v1;

import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses and renders canonical text for Algites version scheme v1. */
public final class AIcAlgitesVersionTextV1 {
	private static final Pattern VERSION_PATTERN = Pattern.compile(
			"^([0-9]+(?:\\.[0-9]+)*)\\.([0-9]+)(?:-(SNAPSHOT)|-rc([0-9]+))?$",
			Pattern.CASE_INSENSITIVE
	);

	private AIcAlgitesVersionTextV1() { /* utility class */ }

	@Nonnull
	public static AIiAlgitesVersionV1 parse(@Nonnull final String aValue) {
		String locValue = Objects.requireNonNull(aValue, "value must not be null").trim();
		Matcher locMatcher = VERSION_PATTERN.matcher(locValue);
		if (!locMatcher.matches()) {
			throw new IllegalArgumentException("Invalid Algites version v1 text '" + locValue + "'");
		}
		String locReleaseLineVersion = locMatcher.group(1);
		int locRevision = Integer.parseInt(locMatcher.group(2));
		if (locMatcher.group(3) != null) {
			return AIcAlgitesVersionSchemeV1.snapshot(locReleaseLineVersion, locRevision, null);
		}
		if (locMatcher.group(4) != null) {
			return AIcAlgitesVersionSchemeV1.releaseCandidate(
					locReleaseLineVersion,
					locRevision,
					Long.parseLong(locMatcher.group(4))
			);
		}
		return AIcAlgitesVersionSchemeV1.release(locReleaseLineVersion, locRevision);
	}

	@Nonnull
	public static String render(@Nonnull final AIiAlgitesVersionV1 aVersion) {
		Objects.requireNonNull(aVersion, "version must not be null");
		String locBase = aVersion.baseVersionText();
		return switch (aVersion.qualifierKind()) {
			case RELEASE -> locBase;
			case SNAPSHOT -> locBase + "-SNAPSHOT";
			case RC -> locBase + "-rc" + (aVersion.qualifierSequence() == null ? 0 : aVersion.qualifierSequence());
		};
	}
}
