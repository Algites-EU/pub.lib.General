package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Parser for compact portable version-requirement boundary expressions. */
public final class AIcVersionRequirementParser {
	private AIcVersionRequirementParser() { /* utility class */ }

	@Nonnull
	public static AIiVersionBound parseMinimum(@Nonnull final String aExpression) {
		return parseBoundary(aExpression, true);
	}

	@Nonnull
	public static AIiVersionBound parseMaximum(@Nonnull final String aExpression) {
		return parseBoundary(aExpression, false);
	}

	@Nonnull
	private static AIiVersionBound parseBoundary(@Nonnull final String aExpression, final boolean aMinimum) {
		String locExpression = aExpression.trim();
		if (locExpression.isEmpty()) {
			throw new IllegalArgumentException("Version boundary expression must not be blank");
		}

		String[] locOperators = aMinimum
				? new String[] {">=", ">", "<=", "<"}
				: new String[] {"<=", "<", ">=", ">"};

		for (String locOperator : locOperators) {
			if (locExpression.startsWith(locOperator)) {
				validatePrefixOperator(locOperator, aMinimum);
				return new AIcVersionBound(locExpression.substring(locOperator.length()), locOperator.length() == 2);
			}
			if (locExpression.endsWith(locOperator)) {
				validateSuffixOperator(locOperator, aMinimum);
				return new AIcVersionBound(locExpression.substring(0, locExpression.length() - locOperator.length()), locOperator.length() == 2);
			}
		}
		throw new IllegalArgumentException("Version boundary must contain a comparison operator");
	}

	private static void validatePrefixOperator(@Nonnull final String aOperator, final boolean aMinimum) {
		boolean locValid = aMinimum ? aOperator.startsWith(">") : aOperator.startsWith("<");
		if (!locValid) {
			throw new IllegalArgumentException("Comparison operator points in the wrong direction for this boundary");
		}
	}

	private static void validateSuffixOperator(@Nonnull final String aOperator, final boolean aMinimum) {
		boolean locValid = aMinimum ? aOperator.startsWith("<") : aOperator.startsWith(">");
		if (!locValid) {
			throw new IllegalArgumentException("Comparison operator points in the wrong direction for this boundary");
		}
	}
}
