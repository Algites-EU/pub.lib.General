package eu.algites.lib.common.version;

import eu.algites.lib.common.interval.AIcInterval;
import eu.algites.lib.common.interval.AInIntervalBoundary;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcVersionInterval}
 * </p>
 * <p>
 * Description: Type-safe interval implementation for {@link AIcVersion} values.
 * </p>
 * <p>
 * Notes:
 * </p>
 * <ul>
 *   <li>This class provides interval operations that can use a specific {@link AIiVersionScheme} comparator.</li>
 *   <li>The base {@link AIcInterval} validation uses {@link AIcVersion#compareTo(AIcVersion)}; for scheme-specific
 *       ordering you should use the constructor that accepts a {@link AIiVersionScheme}.</li>
 * </ul>
 *
 * @author linhart1
 * @date 28.01.26
 */
public class AIcVersionInterval extends AIcInterval<AIcVersion> {

	/**
	 * Creates a new version interval validated using {@link AIcVersion#compareTo(AIcVersion)} (default scheme).
	 *
	 * @param aLeftBoundary left boundary
	 * @param aLeftValue left value
	 * @param aRightBoundary right boundary
	 * @param aRightValue right value
	 */
	public AIcVersionInterval(
			@Nonnull final AInIntervalBoundary aLeftBoundary,
			@Nullable final AIcVersion aLeftValue,
			@Nonnull final AInIntervalBoundary aRightBoundary,
			@Nullable final AIcVersion aRightValue
	) {
		super(aLeftBoundary, aLeftValue, aRightBoundary, aRightValue);
	}

	/**
	 * Creates a new version interval validated using the comparator of the provided scheme.
	 *
	 * @param aLeftBoundary left boundary
	 * @param aLeftValue left value
	 * @param aRightBoundary right boundary
	 * @param aRightValue right value
	 * @param aVersionScheme scheme defining ordering semantics
	 */
	public AIcVersionInterval(
			@Nonnull final AInIntervalBoundary aLeftBoundary,
			@Nullable final AIcVersion aLeftValue,
			@Nonnull final AInIntervalBoundary aRightBoundary,
			@Nullable final AIcVersion aRightValue,
			@Nonnull final AIiVersionScheme aVersionScheme
	) {
		super(aLeftBoundary, aLeftValue, aRightBoundary, aRightValue, true);
		validateIntervalUsingScheme(this, aVersionScheme);
	}

	/**
	 * Returns {@code true} if the given value is contained within this interval using the comparator from the scheme.
	 *
	 * @param aValue value to test
	 * @param aVersionScheme scheme defining ordering semantics
	 * @return {@code true} if contained
	 */
	public boolean contains(@Nonnull final AIcVersion aValue, @Nonnull final AIiVersionScheme aVersionScheme) {
		Objects.requireNonNull(aValue, "Value must not be null");
		AIiVersionComparator locComparator = Objects.requireNonNull(aVersionScheme, "Version scheme must not be null").versionComparator();

		if (!isLeftUnbounded()) {
			AIcVersion locLeft = getLeftValue();
			int locCmp = locComparator.compare(aValue, locLeft);
			if (locCmp < 0) {
				return false;
			}
			if (locCmp == 0 && getLeftBoundary().isOpen()) {
				return false;
			}
		}

		if (!isRightUnbounded()) {
			AIcVersion locRight = getRightValue();
			int locCmp = locComparator.compare(aValue, locRight);
			if (locCmp > 0) {
				return false;
			}
			if (locCmp == 0 && getRightBoundary().isOpen()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns {@code true} if this interval overlaps the other interval using the comparator from the scheme.
	 *
	 * @param aOther other interval
	 * @param aVersionScheme scheme defining ordering semantics
	 * @return {@code true} if overlap exists
	 */
	public boolean overlaps(@Nonnull final AIcVersionInterval aOther, @Nonnull final AIiVersionScheme aVersionScheme) {
		Objects.requireNonNull(aOther, "Other interval must not be null");
		return tryIntersect(aOther, aVersionScheme) != null;
	}

	/**
	 * Computes intersection of this interval with another interval using the comparator from the scheme.
	 *
	 * @param aOther other interval
	 * @param aVersionScheme scheme defining ordering semantics
	 * @return intersection
	 * @throws IllegalArgumentException when intervals do not overlap
	 */
	@Nonnull
	public AIcVersionInterval intersect(@Nonnull final AIcVersionInterval aOther, @Nonnull final AIiVersionScheme aVersionScheme) {
		AIcVersionInterval locResult = tryIntersect(aOther, aVersionScheme);
		if (locResult == null) {
			throw new IllegalArgumentException("Intervals do not overlap");
		}
		return locResult;
	}

	/**
	 * Attempts to compute intersection of this interval with another interval using the comparator from the scheme.
	 *
	 * @param aOther other interval
	 * @param aVersionScheme scheme defining ordering semantics
	 * @return intersection or {@code null} if no overlap
	 */
	@Nullable
	public AIcVersionInterval tryIntersect(@Nonnull final AIcVersionInterval aOther, @Nonnull final AIiVersionScheme aVersionScheme) {
		Objects.requireNonNull(aOther, "Other interval must not be null");
		AIiVersionComparator locComparator = Objects.requireNonNull(aVersionScheme, "Version scheme must not be null").versionComparator();

		Endpoint locNewLeft = maxLeftEndpoint(this, aOther, locComparator);
		Endpoint locNewRight = minRightEndpoint(this, aOther, locComparator);

		if (!isSatisfiable(locNewLeft, locNewRight, locComparator)) {
			return null;
		}

		return new AIcVersionInterval(
				locNewLeft.boundary,
				locNewLeft.value,
				locNewRight.boundary,
				locNewRight.value,
				aVersionScheme
		);
	}

	private boolean isLeftUnbounded() {
		return getLeftBoundary().isBoundaryValueIgnored();
	}

	private boolean isRightUnbounded() {
		return getRightBoundary().isBoundaryValueIgnored();
	}

	private static void validateIntervalUsingScheme(@Nonnull final AIcVersionInterval aInterval, @Nonnull final AIiVersionScheme aVersionScheme) {
		Objects.requireNonNull(aInterval, "Interval must not be null");
		AIiVersionComparator locComparator = Objects.requireNonNull(aVersionScheme, "Version scheme must not be null").versionComparator();

		AInIntervalBoundary locLeftBoundary = Objects.requireNonNull(aInterval.getLeftBoundary(), "Left boundary must not be null");
		AInIntervalBoundary locRightBoundary = Objects.requireNonNull(aInterval.getRightBoundary(), "Right boundary must not be null");

		AIcVersion locLeftValue = aInterval.getLeftValue();
		AIcVersion locRightValue = aInterval.getRightValue();

		if (!locLeftBoundary.isBoundaryValueIgnored() && locLeftValue == null) {
			throw new IllegalArgumentException("Left value must not be null when left boundary does not ignore the boundary value");
		}

		if (!locRightBoundary.isBoundaryValueIgnored() && locRightValue == null) {
			throw new IllegalArgumentException("Right value must not be null when right boundary does not ignore the boundary value");
		}

		if (locLeftBoundary.isBoundaryValueIgnored() || locRightBoundary.isBoundaryValueIgnored()) {
			return;
		}

		int locCmp = locComparator.compare(locLeftValue, locRightValue);

		if (locCmp > 0) {
			throw new IllegalArgumentException("Invalid interval: left value is greater than right value");
		}

		if (locCmp == 0) {
			boolean locValidPoint = !locLeftBoundary.isOpen() && !locRightBoundary.isOpen();
			if (!locValidPoint) {
				throw new IllegalArgumentException("Invalid interval: point interval requires both boundaries to be closed");
			}
		}
	}

	private static Endpoint maxLeftEndpoint(
			@Nonnull final AIcVersionInterval aLeft,
			@Nonnull final AIcVersionInterval aRight,
			@Nonnull final AIiVersionComparator aComparator
	) {
		Endpoint locLeftEndpoint = Endpoint.left(aLeft.getLeftBoundary(), aLeft.getLeftValue());
		Endpoint locRightEndpoint = Endpoint.left(aRight.getLeftBoundary(), aRight.getLeftValue());

		if (locLeftEndpoint.boundary.isBoundaryValueIgnored()) {
			return locRightEndpoint;
		}
		if (locRightEndpoint.boundary.isBoundaryValueIgnored()) {
			return locLeftEndpoint;
		}

		int locCmp = aComparator.compare(locLeftEndpoint.value, locRightEndpoint.value);
		if (locCmp > 0) {
			return locLeftEndpoint;
		}
		if (locCmp < 0) {
			return locRightEndpoint;
		}

		boolean locInclusive = !locLeftEndpoint.boundary.isOpen() && !locRightEndpoint.boundary.isOpen();
		AInIntervalBoundary locBoundary = locInclusive ? AInIntervalBoundary.CLOSED : AInIntervalBoundary.OPEN;

		return Endpoint.left(locBoundary, locLeftEndpoint.value);
	}

	private static Endpoint minRightEndpoint(
			@Nonnull final AIcVersionInterval aLeft,
			@Nonnull final AIcVersionInterval aRight,
			@Nonnull final AIiVersionComparator aComparator
	) {
		Endpoint locLeftEndpoint = Endpoint.right(aLeft.getRightBoundary(), aLeft.getRightValue());
		Endpoint locRightEndpoint = Endpoint.right(aRight.getRightBoundary(), aRight.getRightValue());

		if (locLeftEndpoint.boundary.isBoundaryValueIgnored()) {
			return locRightEndpoint;
		}
		if (locRightEndpoint.boundary.isBoundaryValueIgnored()) {
			return locLeftEndpoint;
		}

		int locCmp = aComparator.compare(locLeftEndpoint.value, locRightEndpoint.value);
		if (locCmp < 0) {
			return locLeftEndpoint;
		}
		if (locCmp > 0) {
			return locRightEndpoint;
		}

		boolean locInclusive = !locLeftEndpoint.boundary.isOpen() && !locRightEndpoint.boundary.isOpen();
		AInIntervalBoundary locBoundary = locInclusive ? AInIntervalBoundary.CLOSED : AInIntervalBoundary.OPEN;

		return Endpoint.right(locBoundary, locLeftEndpoint.value);
	}

	private static boolean isSatisfiable(
			@Nonnull final Endpoint aLeft,
			@Nonnull final Endpoint aRight,
			@Nonnull final AIiVersionComparator aComparator
	) {
		if (aLeft.boundary.isBoundaryValueIgnored() || aRight.boundary.isBoundaryValueIgnored()) {
			return true;
		}

		int locCmp = aComparator.compare(aLeft.value, aRight.value);
		if (locCmp < 0) {
			return true;
		}
		if (locCmp > 0) {
			return false;
		}

		return !aLeft.boundary.isOpen() && !aRight.boundary.isOpen();
	}

	private static final class Endpoint {

		@Nonnull
		private final AInIntervalBoundary boundary;

		@Nullable
		private final AIcVersion value;

		private Endpoint(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final AIcVersion aValue) {
			boundary = Objects.requireNonNull(aBoundary, "Boundary must not be null");
			value = aValue;
		}

		@Nonnull
		public static Endpoint left(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final AIcVersion aValue) {
			return new Endpoint(aBoundary, aValue);
		}

		@Nonnull
		public static Endpoint right(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final AIcVersion aValue) {
			return new Endpoint(aBoundary, aValue);
		}
	}
}
