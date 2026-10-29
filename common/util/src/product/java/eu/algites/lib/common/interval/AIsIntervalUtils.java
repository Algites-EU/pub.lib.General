package eu.algites.lib.common.interval;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * <p>
 * Title: {@link AIsIntervalUtils}
 * </p>
 * <p>
 * Description: Utility methods for working with {@link AIiInterval} instances.
 * </p>
 * <p>
 * The utilities are designed for generic comparable values, including version objects
 * that implement {@link Comparable}.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIsIntervalUtils {

	private AIsIntervalUtils() {
		/* utility class */
	}

	/**
	 * Validates the interval against the contract of {@link AIiInterval} and basic ordering rules.
	 *
	 * @param aInterval interval to validate
	 */
	public static void validateInterval(@Nonnull final AIiInterval<?> aInterval) {
		Objects.requireNonNull(aInterval, "Interval must not be null");

		AInIntervalBoundary locLeftBoundary = Objects.requireNonNull(aInterval.getLeftBoundary(), "Left boundary must not be null");
		AInIntervalBoundary locRightBoundary = Objects.requireNonNull(aInterval.getRightBoundary(), "Right boundary must not be null");

		Object locLeftValue = aInterval.getLeftValue();
		Object locRightValue = aInterval.getRightValue();

		if (!locLeftBoundary.isBoundaryValueIgnored() && locLeftValue == null) {
			throw new IllegalArgumentException("Left value must not be null when left boundary does not ignore the boundary value");
		}

		if (!locRightBoundary.isBoundaryValueIgnored() && locRightValue == null) {
			throw new IllegalArgumentException("Right value must not be null when right boundary does not ignore the boundary value");
		}

		if (locLeftBoundary.isBoundaryValueIgnored() || locRightBoundary.isBoundaryValueIgnored()) {
			return;
		}

		@SuppressWarnings("unchecked")
		Comparable<Object> locLeftComparable = (Comparable<Object>) locLeftValue;
		int locCmp = locLeftComparable.compareTo(locRightValue);

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

	/**
	 * @param aInterval interval
	 * @return {@code true} if left side is unbounded
	 */
	public static boolean isLeftUnbounded(@Nonnull final AIiInterval<?> aInterval) {
		Objects.requireNonNull(aInterval, "Interval must not be null");
		return aInterval.getLeftBoundary().isBoundaryValueIgnored();
	}

	/**
	 * @param aInterval interval
	 * @return {@code true} if right side is unbounded
	 */
	public static boolean isRightUnbounded(@Nonnull final AIiInterval<?> aInterval) {
		Objects.requireNonNull(aInterval, "Interval must not be null");
		return aInterval.getRightBoundary().isBoundaryValueIgnored();
	}

	/**
	 * Returns {@code true} if {@code aValue} is contained in {@code aInterval}.
	 *
	 * @param aInterval interval
	 * @param aValue value
	 * @param <T> value type
	 * @return {@code true} if contained
	 */
	public static <T extends Comparable> boolean contains(@Nonnull final AIiInterval<T> aInterval, @Nonnull final T aValue) {
		Objects.requireNonNull(aInterval, "Interval must not be null");
		Objects.requireNonNull(aValue, "Value must not be null");

		if (!isLeftUnbounded(aInterval)) {
			T locLeft = aInterval.getLeftValue();
			int locCmp = aValue.compareTo(locLeft);
			if (locCmp < 0) {
				return false;
			}
			if (locCmp == 0 && aInterval.getLeftBoundary().isOpen()) {
				return false;
			}
		}

		if (!isRightUnbounded(aInterval)) {
			T locRight = aInterval.getRightValue();
			int locCmp = aValue.compareTo(locRight);
			if (locCmp > 0) {
				return false;
			}
			if (locCmp == 0 && aInterval.getRightBoundary().isOpen()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns {@code true} if two intervals overlap (have a non-empty intersection).
	 *
	 * @param aLeft first interval
	 * @param aRight second interval
	 * @param <T> value type
	 * @return {@code true} if overlap exists
	 */
	public static <T extends Comparable> boolean overlaps(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Objects.requireNonNull(aLeft, "Left interval must not be null");
		Objects.requireNonNull(aRight, "Right interval must not be null");

		return tryIntersect(aLeft, aRight) != null;
	}

	/**
	 * Computes intersection of two intervals.
	 *
	 * @param aLeft first interval
	 * @param aRight second interval
	 * @param <T> value type
	 * @return intersection
	 * @throws IllegalArgumentException when intervals do not overlap
	 */
	@Nonnull
	public static <T extends Comparable> AIcInterval<T> intersect(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		AIcInterval<T> locResult = tryIntersect(aLeft, aRight);
		if (locResult == null) {
			throw new IllegalArgumentException("Intervals do not overlap");
		}
		return locResult;
	}

	/**
	 * Attempts to compute intersection of two intervals.
	 *
	 * @param aLeft first interval
	 * @param aRight second interval
	 * @param <T> value type
	 * @return intersection or {@code null} if no overlap
	 */
	@Nullable
	public static <T extends Comparable> AIcInterval<T> tryIntersect(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Objects.requireNonNull(aLeft, "Left interval must not be null");
		Objects.requireNonNull(aRight, "Right interval must not be null");

		Endpoint<T> locNewLeft = maxLeftEndpoint(aLeft, aRight);
		Endpoint<T> locNewRight = minRightEndpoint(aLeft, aRight);

		if (!isSatisfiable(locNewLeft, locNewRight)) {
			return null;
		}

		return new AIcInterval<>(
				locNewLeft.boundary,
				locNewLeft.value,
				locNewRight.boundary,
				locNewRight.value
		);
	}

	/**
	 * Returns {@code true} if an interval is satisfiable (not empty) based on its endpoints.
	 *
	 * @param aInterval interval
	 * @param <T> value type
	 * @return {@code true} if satisfiable
	 */
	public static <T extends Comparable> boolean isSatisfiable(@Nonnull final AIiInterval<T> aInterval) {
		Objects.requireNonNull(aInterval, "Interval must not be null");
		Endpoint<T> locLeft = Endpoint.left(aInterval.getLeftBoundary(), aInterval.getLeftValue());
		Endpoint<T> locRight = Endpoint.right(aInterval.getRightBoundary(), aInterval.getRightValue());
		return isSatisfiable(locLeft, locRight);
	}

	/**
	 * Produces a concise string representation using boundary symbols.
	 *
	 * @param aInterval interval
	 * @return string representation
	 */
	@Nonnull
	public static String toStringRepresentation(@Nonnull final AIiInterval<?> aInterval) {
		Objects.requireNonNull(aInterval, "Interval must not be null");

		AInIntervalBoundary locLeftBoundary = aInterval.getLeftBoundary();
		AInIntervalBoundary locRightBoundary = aInterval.getRightBoundary();

		StringBuilder locBuilder = new StringBuilder();

		locBuilder.append(locLeftBoundary.getLeftBoundaryStringRepresentation());
		if (!locLeftBoundary.isBoundaryValueIgnored()) {
			locBuilder.append(String.valueOf(aInterval.getLeftValue()));
		}
		locBuilder.append(", ");
		if (!locRightBoundary.isBoundaryValueIgnored()) {
			locBuilder.append(String.valueOf(aInterval.getRightValue()));
		}
		locBuilder.append(locRightBoundary.getRightBoundaryStringRepresentation());

		return locBuilder.toString();
	}

	/**
	 * Compares two intervals by their left endpoints, then by right endpoints.
	 *
	 * @param aLeft first interval
	 * @param aRight second interval
	 * @param <T> value type
	 * @return comparator result
	 */
	public static <T extends Comparable> int compare(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Objects.requireNonNull(aLeft, "Left interval must not be null");
		Objects.requireNonNull(aRight, "Right interval must not be null");

		int locLeftCmp = compareLeftEndpoints(aLeft, aRight);
		if (locLeftCmp != 0) {
			return locLeftCmp;
		}
		return compareRightEndpoints(aLeft, aRight);
	}

	/**
	 * Returns {@code true} if {@code aLeft} is strictly before {@code aRight} (no overlap and left ends before right starts).
	 *
	 * @param aLeft left interval
	 * @param aRight right interval
	 * @param <T> value type
	 * @return {@code true} if strictly before
	 */
	public static <T extends Comparable> boolean isStrictlyBefore(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Objects.requireNonNull(aLeft, "Left interval must not be null");
		Objects.requireNonNull(aRight, "Right interval must not be null");

		if (isRightUnbounded(aLeft) || isLeftUnbounded(aRight)) {
			return false;
		}

		T locLeftRight = aLeft.getRightValue();
		T locRightLeft = aRight.getLeftValue();

		int locCmp = locLeftRight.compareTo(locRightLeft);
		if (locCmp < 0) {
			return true;
		}
		if (locCmp > 0) {
			return false;
		}

		boolean locTouchingIsGap = aLeft.getRightBoundary().isOpen() || aRight.getLeftBoundary().isOpen();
		return locTouchingIsGap;
	}

	private static <T extends Comparable> boolean isSatisfiable(@Nonnull final Endpoint<T> aLeft, @Nonnull final Endpoint<T> aRight) {
		if (aLeft.boundary.isBoundaryValueIgnored() || aRight.boundary.isBoundaryValueIgnored()) {
			return true;
		}

		T locLeftValue = aLeft.value;
		T locRightValue = aRight.value;

		int locCmp = locLeftValue.compareTo(locRightValue);
		if (locCmp < 0) {
			return true;
		}
		if (locCmp > 0) {
			return false;
		}

		return !aLeft.boundary.isOpen() && !aRight.boundary.isOpen();
	}

	private static <T extends Comparable> Endpoint<T> maxLeftEndpoint(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Endpoint<T> locLeftEndpoint = Endpoint.left(aLeft.getLeftBoundary(), aLeft.getLeftValue());
		Endpoint<T> locRightEndpoint = Endpoint.left(aRight.getLeftBoundary(), aRight.getLeftValue());

		if (locLeftEndpoint.boundary.isBoundaryValueIgnored()) {
			return locRightEndpoint;
		}
		if (locRightEndpoint.boundary.isBoundaryValueIgnored()) {
			return locLeftEndpoint;
		}

		int locCmp = locLeftEndpoint.value.compareTo(locRightEndpoint.value);
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

	private static <T extends Comparable> Endpoint<T> minRightEndpoint(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		Endpoint<T> locLeftEndpoint = Endpoint.right(aLeft.getRightBoundary(), aLeft.getRightValue());
		Endpoint<T> locRightEndpoint = Endpoint.right(aRight.getRightBoundary(), aRight.getRightValue());

		if (locLeftEndpoint.boundary.isBoundaryValueIgnored()) {
			return locRightEndpoint;
		}
		if (locRightEndpoint.boundary.isBoundaryValueIgnored()) {
			return locLeftEndpoint;
		}

		int locCmp = locLeftEndpoint.value.compareTo(locRightEndpoint.value);
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

	private static <T extends Comparable> int compareLeftEndpoints(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		boolean locLeftUnbounded = isLeftUnbounded(aLeft);
		boolean locRightUnbounded = isLeftUnbounded(aRight);

		if (locLeftUnbounded && locRightUnbounded) {
			return 0;
		}
		if (locLeftUnbounded) {
			return -1;
		}
		if (locRightUnbounded) {
			return 1;
		}

		T locLeftValue = aLeft.getLeftValue();
		T locRightValue = aRight.getLeftValue();

		int locCmp = locLeftValue.compareTo(locRightValue);
		if (locCmp != 0) {
			return locCmp;
		}

		boolean locLeftClosed = !aLeft.getLeftBoundary().isOpen();
		boolean locRightClosed = !aRight.getLeftBoundary().isOpen();

		if (locLeftClosed == locRightClosed) {
			return 0;
		}

		return locLeftClosed ? -1 : 1;
	}

	private static <T extends Comparable> int compareRightEndpoints(@Nonnull final AIiInterval<T> aLeft, @Nonnull final AIiInterval<T> aRight) {
		boolean locLeftUnbounded = isRightUnbounded(aLeft);
		boolean locRightUnbounded = isRightUnbounded(aRight);

		if (locLeftUnbounded && locRightUnbounded) {
			return 0;
		}
		if (locLeftUnbounded) {
			return 1;
		}
		if (locRightUnbounded) {
			return -1;
		}

		T locLeftValue = aLeft.getRightValue();
		T locRightValue = aRight.getRightValue();

		int locCmp = locLeftValue.compareTo(locRightValue);
		if (locCmp != 0) {
			return locCmp;
		}

		boolean locLeftClosed = !aLeft.getRightBoundary().isOpen();
		boolean locRightClosed = !aRight.getRightBoundary().isOpen();

		if (locLeftClosed == locRightClosed) {
			return 0;
		}

		return locLeftClosed ? 1 : -1;
	}

	private static final class Endpoint<T extends Comparable> {

		private final AInIntervalBoundary boundary;
		private final T value;

		private Endpoint(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final T aValue) {
			boundary = Objects.requireNonNull(aBoundary, "Boundary must not be null");
			value = aValue;
		}

		private static <T extends Comparable> Endpoint<T> left(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final T aValue) {
			return new Endpoint<>(aBoundary, aValue);
		}

		private static <T extends Comparable> Endpoint<T> right(@Nonnull final AInIntervalBoundary aBoundary, @Nullable final T aValue) {
			return new Endpoint<>(aBoundary, aValue);
		}
	}
}
