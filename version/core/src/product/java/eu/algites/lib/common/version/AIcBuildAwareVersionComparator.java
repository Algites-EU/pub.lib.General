package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcBuildAwareVersionComparator}
 * </p>
 * <p>
 * Description: Comparator wrapper that can split a version string into a "real" version part and a build-identification part.
 * </p>
 * <p>
 * The base comparator compares only the precedence-relevant version part. Optionally, the build-identification
 * part can be compared when the base comparison result is equal.
 * </p>
 *
 * @author linhart1
 * @dateTopics: version, build-metadata
 * @date 28.01.26
 */
public final class AIcBuildAwareVersionComparator implements AIiVersionComparator, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	private final AIiVersionComparator baseComparator;

	@Nonnull
	private final String buildDelimiter;

	private final boolean versionBeforeBuild;

	@Nonnull
	private final AInVersionBuildComparisonPolicy buildComparisonPolicy;

	public AIcBuildAwareVersionComparator(
			@Nonnull final AIiVersionComparator aBaseComparator,
			@Nonnull final String aBuildDelimiter,
			final boolean aVersionBeforeBuild,
			@Nonnull final AInVersionBuildComparisonPolicy aBuildComparisonPolicy
	) {
		baseComparator = Objects.requireNonNull(aBaseComparator, "Base comparator must not be null");
		buildDelimiter = Objects.requireNonNull(aBuildDelimiter, "Build delimiter must not be null");
		versionBeforeBuild = aVersionBeforeBuild;
		buildComparisonPolicy = Objects.requireNonNull(aBuildComparisonPolicy, "Build comparison policy must not be null");
	}

	@Override
	public int compare(@Nonnull final AIcVersion aLeft, @Nonnull final AIcVersion aRight) {
		Objects.requireNonNull(aLeft, "Left version must not be null");
		Objects.requireNonNull(aRight, "Right version must not be null");

		AIiVersionSchemeTextParts locLeftParts = splitText(aLeft.getOriginalText());
		AIiVersionSchemeTextParts locRightParts = splitText(aRight.getOriginalText());

		AIcVersion locLeftVersion = new AIcVersion(locLeftParts.versionText());
		AIcVersion locRightVersion = new AIcVersion(locRightParts.versionText());

		int locCmp = baseComparator.compare(locLeftVersion, locRightVersion);
		if (locCmp != 0) {
			return locCmp;
		}

		if (buildComparisonPolicy == AInVersionBuildComparisonPolicy.IGNORE) {
			return 0;
		}

		return compareBuildTokens(locLeftParts.buildText(), locRightParts.buildText());
	}

	@Nonnull
	private AIiVersionSchemeTextParts splitText(@Nonnull final String aText) {
		Objects.requireNonNull(aText, "Text must not be null");

		if (buildDelimiter.isEmpty()) {
			return new AIrVersionSchemeTextParts(aText, "");
		}

		int locIndex = aText.indexOf(buildDelimiter);
		if (locIndex < 0) {
			return new AIrVersionSchemeTextParts(aText, "");
		}

		String locFirst = aText.substring(0, locIndex);
		String locSecond = aText.substring(locIndex + buildDelimiter.length());

		if (versionBeforeBuild) {
			return new AIrVersionSchemeTextParts(locFirst, locSecond);
		}
		return new AIrVersionSchemeTextParts(locSecond, locFirst);
	}

	private int compareBuildTokens(@Nonnull final String aLeftBuildText, @Nonnull final String aRightBuildText) {
		Objects.requireNonNull(aLeftBuildText, "Left build text must not be null");
		Objects.requireNonNull(aRightBuildText, "Right build text must not be null");

		if (aLeftBuildText.isEmpty() && aRightBuildText.isEmpty()) {
			return 0;
		}
		if (aLeftBuildText.isEmpty()) {
			return -1;
		}
		if (aRightBuildText.isEmpty()) {
			return 1;
		}

		List<Object> locLeftTokens = tokenizeBuildText(aLeftBuildText);
		List<Object> locRightTokens = tokenizeBuildText(aRightBuildText);

		int locMax = Math.max(locLeftTokens.size(), locRightTokens.size());
		for (int locIndex = 0; locIndex < locMax; locIndex++) {
			Object locLeft = locIndex < locLeftTokens.size() ? locLeftTokens.get(locIndex) : null;
			Object locRight = locIndex < locRightTokens.size() ? locRightTokens.get(locIndex) : null;

			if (locLeft == null && locRight == null) {
				return 0;
			}
			if (locLeft == null) {
				return -1;
			}
			if (locRight == null) {
				return 1;
			}

			int locCmp = compareBuildToken(locLeft, locRight);
			if (locCmp != 0) {
				return locCmp;
			}
		}

		return 0;
	}

	@Nonnull
	private List<Object> tokenizeBuildText(@Nonnull final String aBuildText) {
		Objects.requireNonNull(aBuildText, "Build text must not be null");

		List<Object> locTokens = new ArrayList<>();
		String[] locParts = aBuildText.split("[^A-Za-z0-9]+");
		for (String locPart : locParts) {
			if (locPart == null || locPart.isEmpty()) {
				continue;
			}
			Object locToken = parseNumericOrString(locPart);
			locTokens.add(locToken);
		}
		return locTokens;
	}

	@Nonnull
	private Object parseNumericOrString(@Nonnull final String aTokenText) {
		Objects.requireNonNull(aTokenText, "Token text must not be null");

		boolean locAllDigits = true;
		for (int locIndex = 0; locIndex < aTokenText.length(); locIndex++) {
			char locCh = aTokenText.charAt(locIndex);
			if (locCh < '0' || locCh > '9') {
				locAllDigits = false;
				break;
			}
		}

		if (locAllDigits) {
			try {
				return new BigInteger(aTokenText);
			} catch (NumberFormatException aEx) {
				return aTokenText;
			}
		}

		return aTokenText;
	}

	private int compareBuildToken(@Nonnull final Object aLeft, @Nonnull final Object aRight) {
		Objects.requireNonNull(aLeft, "Left token must not be null");
		Objects.requireNonNull(aRight, "Right token must not be null");

		boolean locLeftNumeric = aLeft instanceof BigInteger;
		boolean locRightNumeric = aRight instanceof BigInteger;

		if (locLeftNumeric && locRightNumeric) {
			return ((BigInteger) aLeft).compareTo((BigInteger) aRight);
		}
		if (locLeftNumeric != locRightNumeric) {
			return locLeftNumeric ? -1 : 1;
		}

		String locLeftText = aLeft.toString();
		String locRightText = aRight.toString();
		return locLeftText.compareToIgnoreCase(locRightText);
	}
}
