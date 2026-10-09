package eu.algites.lib.common.version;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcCalverLikeVersionComparator}
 * </p>
 * <p>
 * Description: Example comparator for calendar versioning (CalVer).
 * </p>
 * <p>
 * Pattern example (extendability):
 * </p>
 * <ul>
 *   <li>Create a comparator implementing {@link AIiVersionComparator}.</li>
 *   <li>Optionally add a purpose (e.g. enum constant) or use {@link AIcCustomVersionScheme}.</li>
 *   <li>Use {@link AIsVersionComparator#compare(AIcVersion, AIcVersion, AIiVersionComparator)}.</li>
 * </ul>
 * <p>
 * This implementation tries to extract up to 3 numeric segments (year, month, patch) from the version text
 * and compares them numerically. If extraction fails for either side, it falls back to Maven-like comparison.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcCalverLikeVersionComparator implements AIiVersionComparator, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final AIcVersion aLeft, final AIcVersion aRight) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");

		List<Long> locLeftParts = extractNumericParts(aLeft);
		List<Long> locRightParts = extractNumericParts(aRight);

		if (locLeftParts.isEmpty() || locRightParts.isEmpty()) {
			return AIsVersionComparator.compareMavenLike(aLeft, aRight);
		}

		int locMax = Math.max(locLeftParts.size(), locRightParts.size());
		for (int locIndex = 0; locIndex < locMax; locIndex++) {
			long locLeft = locIndex < locLeftParts.size() ? locLeftParts.get(locIndex) : 0L;
			long locRight = locIndex < locRightParts.size() ? locRightParts.get(locIndex) : 0L;

			int locCmp = Long.compare(locLeft, locRight);
			if (locCmp != 0) {
				return locCmp;
			}
		}

		return AIsVersionComparator.compareMavenLike(aLeft, aRight);
	}

	private static List<Long> extractNumericParts(final AIcVersion aVersion) {
		List<Long> locParts = new ArrayList<>(3);

		for (AIcVersionToken locToken : aVersion.getTokens()) {
			if (locToken.getTokenType() != AInVersionTokenType.ALPHANUMERIC) {
				continue;
			}

			String locText = locToken.getText();
			int locIndex = 0;

			while (locIndex < locText.length() && locParts.size() < 3) {
				char locChar = locText.charAt(locIndex);
				if (locChar < '0' || locChar > '9') {
					locIndex++;
					continue;
				}

				int locStart = locIndex;
				locIndex++;
				while (locIndex < locText.length()) {
					char locNext = locText.charAt(locIndex);
					if (locNext < '0' || locNext > '9') {
						break;
					}
					locIndex++;
				}

				String locNum = locText.substring(locStart, locIndex);
				try {
					locParts.add(Long.parseLong(locNum));
				} catch (NumberFormatException locException) {
					return List.of();
				}
			}
		}

		return locParts;
	}
}
