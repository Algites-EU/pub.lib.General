package eu.algites.lib.common.version.scheme.pep440;

import eu.algites.lib.common.version.AIcVersion;
import eu.algites.lib.common.version.AIiVersionComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Comparator for the commonly used PEP 440 release/pre/dev/post ordering model. */
public final class AIcPep440VersionComparator implements AIiVersionComparator {
	private static final Pattern VERSION_PATTERN = Pattern.compile(
			"^(?:(\\d+)!)?(\\d+(?:\\.\\d+)*)(?:(a|b|rc)(\\d+))?(?:\\.post(\\d+))?(?:\\.dev(\\d+))?(?:\\+.*)?$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public int compare(final AIcVersion aLeft, final AIcVersion aRight) {
		AIrParts locLeft = parse(aLeft.getOriginalText());
		AIrParts locRight = parse(aRight.getOriginalText());
		int locResult = Integer.compare(locLeft.epoch, locRight.epoch);
		if (locResult != 0) return locResult;
		locResult = compareRelease(locLeft.release, locRight.release);
		if (locResult != 0) return locResult;
		locResult = Integer.compare(stageRank(locLeft.preKind, locLeft.dev), stageRank(locRight.preKind, locRight.dev));
		if (locResult != 0) return locResult;
		locResult = compareNullableInt(locLeft.preNumber, locRight.preNumber);
		if (locResult != 0) return locResult;
		locResult = compareNullableInt(locLeft.dev, locRight.dev);
		if (locResult != 0) return locResult;
		return compareNullableInt(locLeft.post, locRight.post);
	}

	private static AIrParts parse(final String aText) {
		Matcher locMatcher = VERSION_PATTERN.matcher(aText.trim().toLowerCase(Locale.ROOT));
		if (!locMatcher.matches()) {
			throw new IllegalArgumentException("Unsupported PEP 440 version: " + aText);
		}
		List<Integer> locRelease = new ArrayList<>();
		for (String locPart : locMatcher.group(2).split("\\.")) locRelease.add(Integer.parseInt(locPart));
		return new AIrParts(
				locMatcher.group(1) == null ? 0 : Integer.parseInt(locMatcher.group(1)),
				locRelease,
				locMatcher.group(3), integerOrNull(locMatcher.group(4)),
				integerOrNull(locMatcher.group(5)), integerOrNull(locMatcher.group(6)));
	}

	private static int stageRank(final String aPreKind, final Integer aDev) {
		if (aPreKind == null && aDev != null) return 0;
		if ("a".equals(aPreKind)) return 1;
		if ("b".equals(aPreKind)) return 2;
		if ("rc".equals(aPreKind)) return 3;
		return 4;
	}

	private static int compareRelease(final List<Integer> aLeft, final List<Integer> aRight) {
		int locLength = Math.max(aLeft.size(), aRight.size());
		for (int locIndex = 0; locIndex < locLength; locIndex++) {
			int locLeft = locIndex < aLeft.size() ? aLeft.get(locIndex) : 0;
			int locRight = locIndex < aRight.size() ? aRight.get(locIndex) : 0;
			int locResult = Integer.compare(locLeft, locRight);
			if (locResult != 0) return locResult;
		}
		return 0;
	}

	private static Integer integerOrNull(final String aText) {
		return aText == null ? null : Integer.valueOf(aText);
	}

	private static int compareNullableInt(final Integer aLeft, final Integer aRight) {
		if (aLeft == null && aRight == null) return 0;
		if (aLeft == null) return 1;
		if (aRight == null) return -1;
		return Integer.compare(aLeft, aRight);
	}

	private record AIrParts(int epoch, List<Integer> release, String preKind, Integer preNumber, Integer post, Integer dev) {}
}
