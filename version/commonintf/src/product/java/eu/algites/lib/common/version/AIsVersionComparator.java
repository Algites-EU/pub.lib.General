package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIsVersionComparator}
 * </p>
 * <p>
 * Description: Compares versions using token-based parsing.
 * </p>
 * <p>
 * The comparator is extensible via {@link AIiVersionComparator} implementations, which can be provided by
 * {@link AIiVersionScheme} instances.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIsVersionComparator {

	private static final Map<String, Integer> MAVEN_QUALIFIER_ORDER = createMavenQualifierOrder();

	private AIsVersionComparator() {
		/* utility class */
	}

	/**
	 * Compares two versions using a {@link AIiVersionScheme}. The purpose provides its comparator via
	 * {@link AIiVersionScheme#versionComparator()}.
	 *
	 * @param aLeft left version
	 * @param aRight right version
	 * @param aScheme handling purpose
	 * @return comparison result
	 */
	public static int compare(@Nonnull final AIcVersion aLeft, @Nonnull final AIcVersion aRight, @Nonnull final AIiVersionScheme aScheme) {
		Objects.requireNonNull(aScheme, "Handling purpose must not be null");
		AIiVersionComparator locComparator = aScheme.versionComparator();
		String locBuildDelimiter = aScheme.versionStructure().buildDelimiter();
		boolean locNeedsBuildAwareWrapper = !locBuildDelimiter.isEmpty()
				|| !aScheme.versionStructure().versionBeforeBuild()
				|| aScheme.versionStructure().buildComparisonPolicy() != AInVersionBuildComparisonPolicy.IGNORE;
		if (locNeedsBuildAwareWrapper) {
			locComparator = new AIcBuildAwareVersionComparator(
					locComparator,
					locBuildDelimiter,
					aScheme.versionStructure().versionBeforeBuild(),
					aScheme.versionStructure().buildComparisonPolicy()
			);
		}
		return compare(aLeft, aRight, locComparator);
	}

	/**
	 * Compares two versions using a provided comparator.
	 *
	 * @param aLeft left version
	 * @param aRight right version
	 * @param aComparator comparator to use
	 * @return comparison result
	 */
	public static int compare(@Nonnull final AIcVersion aLeft, @Nonnull final AIcVersion aRight, @Nonnull final AIiVersionComparator aComparator) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");
		Objects.requireNonNull(aComparator, "Comparator must not be null");
		return aComparator.compare(aLeft, aRight);
	}

	/**
	 * Performs Maven-like comparison. This is the canonical implementation for
	 * {@link AIcMavenLikeVersionComparator} and {@link AIcCalverLikeVersionComparator}.
	 *
	 * @param aLeft left version
	 * @param aRight right version
	 * @return comparison result
	 */
	public static int compareMavenLike(@Nonnull final AIcVersion aLeft, @Nonnull final AIcVersion aRight) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");

		List<AIcVersionItem> locLeftItems = normalizeToItemsMavenLike(aLeft.getTokens());
		List<AIcVersionItem> locRightItems = normalizeToItemsMavenLike(aRight.getTokens());

		return compareNormalizedItems(locLeftItems, locRightItems, false);
	}

	/**
	 * Performs SemVer-like comparison. This is the canonical implementation for {@link AIcSemverLikeVersionComparator}.
	 *
	 * @param aLeft left version
	 * @param aRight right version
	 * @return comparison result
	 */
	public static int compareSemverLike(@Nonnull final AIcVersion aLeft, @Nonnull final AIcVersion aRight) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");

		List<AIcVersionItem> locLeftItems = normalizeToItemsSemverLike(aLeft.getTokens());
		List<AIcVersionItem> locRightItems = normalizeToItemsSemverLike(aRight.getTokens());

		return compareNormalizedItems(locLeftItems, locRightItems, true);
	}

	private static int compareNormalizedItems(
			@Nonnull final List<AIcVersionItem> aLeftItems,
			@Nonnull final List<AIcVersionItem> aRightItems,
			final boolean aSemverLike
	) {
		int locMax = Math.max(aLeftItems.size(), aRightItems.size());

		for (int locIndex = 0; locIndex < locMax; locIndex++) {
			AIcVersionItem locLeftItem = locIndex < aLeftItems.size() ? aLeftItems.get(locIndex) : AIcVersionItem.releaseMarker();
			AIcVersionItem locRightItem = locIndex < aRightItems.size() ? aRightItems.get(locIndex) : AIcVersionItem.releaseMarker();

			int locCmp = compareItems(locLeftItem, locRightItem, aSemverLike);
			if (locCmp != 0) {
				return locCmp;
			}
		}

		return 0;
	}

	@Nonnull
	private static List<AIcVersionItem> normalizeToItemsMavenLike(@Nonnull final List<AIcVersionToken> aTokens) {
		return normalizeToItems(aTokens, false);
	}

	@Nonnull
	private static List<AIcVersionItem> normalizeToItemsSemverLike(@Nonnull final List<AIcVersionToken> aTokens) {
		return normalizeToItems(aTokens, true);
	}

	@Nonnull
	private static List<AIcVersionItem> normalizeToItems(@Nonnull final List<AIcVersionToken> aTokens, final boolean aSemverLike) {
		List<AIcVersionItem> locItems = new ArrayList<>();
		boolean locIgnoreRemainderForSemVer = false;

		for (int locIndex = 0; locIndex < aTokens.size(); locIndex++) {
			AIcVersionToken locToken = aTokens.get(locIndex);

			if (aSemverLike && locIgnoreRemainderForSemVer) {
				break;
			}

			if (locToken.getTokenType() == AInVersionTokenType.SEPARATOR) {
				String locSep = locToken.getText();
				if (aSemverLike && locSep.indexOf('+') >= 0) {
					locIgnoreRemainderForSemVer = true;
				}
				continue;
			}

			String locText = locToken.getText();
			List<AIcVersionItem> locSplit = splitAlphaNumeric(locText);
			locItems.addAll(locSplit);
		}

		trimTrailingZerosAndReleaseMarkers(locItems);
		return locItems;
	}

	@Nonnull
	private static List<AIcVersionItem> splitAlphaNumeric(@Nonnull final String aText) {
		List<AIcVersionItem> locItems = new ArrayList<>();

		int locIndex = 0;
		while (locIndex < aText.length()) {
			char locChar = aText.charAt(locIndex);
			boolean locDigit = locChar >= '0' && locChar <= '9';

			int locStart = locIndex;
			locIndex++;

			while (locIndex < aText.length()) {
				char locNext = aText.charAt(locIndex);
				boolean locNextDigit = locNext >= '0' && locNext <= '9';
				if (locNextDigit != locDigit) {
					break;
				}
				locIndex++;
			}

			String locPart = aText.substring(locStart, locIndex);
			if (locDigit) {
				locItems.add(AIcVersionItem.numeric(locPart));
			} else {
				locItems.add(AIcVersionItem.qualifier(locPart));
			}
		}

		return locItems;
	}

	private static void trimTrailingZerosAndReleaseMarkers(@Nonnull final List<AIcVersionItem> aItems) {
		int locEnd = aItems.size() - 1;
		while (locEnd >= 0) {
			AIcVersionItem locItem = aItems.get(locEnd);

			if (locItem.getItemType() == AInVersionItemType.NUMERIC && locItem.getNumericValue() == 0L) {
				aItems.remove(locEnd);
				locEnd--;
				continue;
			}

			if (locItem.getItemType() == AInVersionItemType.RELEASE_MARKER) {
				aItems.remove(locEnd);
				locEnd--;
				continue;
			}

			break;
		}
	}

	private static int compareItems(@Nonnull final AIcVersionItem aLeft, @Nonnull final AIcVersionItem aRight, final boolean aSemverLike) {
		AInVersionItemType locLeftType = aLeft.getItemType();
		AInVersionItemType locRightType = aRight.getItemType();

		if (locLeftType == AInVersionItemType.NUMERIC && locRightType == AInVersionItemType.NUMERIC) {
			return Long.compare(aLeft.getNumericValue(), aRight.getNumericValue());
		}

		if (locLeftType == AInVersionItemType.RELEASE_MARKER || locRightType == AInVersionItemType.RELEASE_MARKER) {
			return compareAgainstReleaseMarker(aLeft, aRight);
		}

		if (locLeftType == AInVersionItemType.NUMERIC && locRightType == AInVersionItemType.QUALIFIER) {
			return 1;
		}

		if (locLeftType == AInVersionItemType.QUALIFIER && locRightType == AInVersionItemType.NUMERIC) {
			return -1;
		}

		return compareQualifiers(aLeft.getQualifierValue(), aRight.getQualifierValue(), aSemverLike);
	}

	private static int compareAgainstReleaseMarker(@Nonnull final AIcVersionItem aLeft, @Nonnull final AIcVersionItem aRight) {
		if (aLeft.getItemType() == AInVersionItemType.RELEASE_MARKER && aRight.getItemType() == AInVersionItemType.RELEASE_MARKER) {
			return 0;
		}

		if (aLeft.getItemType() == AInVersionItemType.RELEASE_MARKER) {
			if (aRight.getItemType() == AInVersionItemType.NUMERIC) {
				return aRight.getNumericValue() == 0L ? 0 : -1;
			}
			return 1;
		}

		if (aRight.getItemType() == AInVersionItemType.RELEASE_MARKER) {
			if (aLeft.getItemType() == AInVersionItemType.NUMERIC) {
				return aLeft.getNumericValue() == 0L ? 0 : 1;
			}
			return -1;
		}

		return 0;
	}

	private static int compareQualifiers(@Nonnull final String aLeft, @Nonnull final String aRight, final boolean aSemverLike) {
		String locLeft = normalizeQualifier(aLeft);
		String locRight = normalizeQualifier(aRight);

		if (aSemverLike) {
			return locLeft.compareTo(locRight);
		}

		Integer locLeftRank = MAVEN_QUALIFIER_ORDER.get(locLeft);
		Integer locRightRank = MAVEN_QUALIFIER_ORDER.get(locRight);

		if (locLeftRank != null || locRightRank != null) {
			int locLeftValue = locLeftRank != null ? locLeftRank : 0;
			int locRightValue = locRightRank != null ? locRightRank : 0;

			int locCmp = Integer.compare(locLeftValue, locRightValue);
			if (locCmp != 0) {
				return locCmp;
			}
		}

		return locLeft.compareTo(locRight);
	}

	@Nonnull
	private static String normalizeQualifier(@Nonnull final String aQualifier) {
		String locLower = aQualifier.toLowerCase(Locale.ROOT);

		if (locLower.equals("ga") || locLower.equals("final") || locLower.equals("release")) {
			return "";
		}

		if (locLower.equals("cr")) {
			return "rc";
		}

		return locLower;
	}

	@Nonnull
	private static Map<String, Integer> createMavenQualifierOrder() {
		Map<String, Integer> locMap = new HashMap<>();
		locMap.put("snapshot", -50);
		locMap.put("alpha", -40);
		locMap.put("a", -40);
		locMap.put("beta", -30);
		locMap.put("b", -30);
		locMap.put("milestone", -20);
		locMap.put("m", -20);
		locMap.put("rc", -10);
		locMap.put("", 0);
		locMap.put("sp", 10);
		return locMap;
	}

	private enum AInVersionItemType {
		NUMERIC,
		QUALIFIER,
		RELEASE_MARKER
	}

	private static final class AIcVersionItem {

		private final AInVersionItemType itemType;
		private final long numericValue;
		private final String qualifierValue;

		private AIcVersionItem(@Nonnull final AInVersionItemType aItemType, final long aNumericValue, final String aQualifierValue) {
			itemType = Objects.requireNonNull(aItemType, "Item type must not be null");
			numericValue = aNumericValue;
			qualifierValue = aQualifierValue;
		}

		@Nonnull
		private static AIcVersionItem numeric(@Nonnull final String aText) {
			Objects.requireNonNull(aText, "Numeric text must not be null");
			long locValue;
			try {
				locValue = Long.parseLong(aText);
			} catch (NumberFormatException locException) {
				locValue = 0L;
			}
			return new AIcVersionItem(AInVersionItemType.NUMERIC, locValue, null);
		}

		@Nonnull
		private static AIcVersionItem qualifier(@Nonnull final String aText) {
			Objects.requireNonNull(aText, "Qualifier text must not be null");
			return new AIcVersionItem(AInVersionItemType.QUALIFIER, 0L, aText);
		}

		@Nonnull
		private static AIcVersionItem releaseMarker() {
			return new AIcVersionItem(AInVersionItemType.RELEASE_MARKER, 0L, null);
		}

		@Nonnull
		private AInVersionItemType getItemType() {
			return itemType;
		}

		private long getNumericValue() {
			return numericValue;
		}

		@Nonnull
		private String getQualifierValue() {
			return qualifierValue != null ? qualifierValue : "";
		}
	}
}
