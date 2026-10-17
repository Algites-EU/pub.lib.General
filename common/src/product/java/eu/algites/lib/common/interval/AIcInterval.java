package eu.algites.lib.common.interval;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcInterval}
 * </p>
 * <p>
 * Description: Default immutable implementation of {@link AIiInterval}.
 * </p>
 * <p>
 * Notes:
 * </p>
 * <ul>
 *   <li>The boundary value may be {@code null} if {@link AInIntervalBoundary#isBoundaryValueIgnored()} returns {@code true}.</li>
 *   <li>This class does not attempt to reinterpret boundary semantics; it follows the contract of {@link AIiInterval}.</li>
 * </ul>
 *
 * @author linhart1
 * @date 26.01.26
 */
public class AIcInterval<T extends Comparable> implements AIiInterval<T>, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	private final AInIntervalBoundary leftBoundary;

	@Nullable
	private final T leftValue;

	@Nonnull
	private final AInIntervalBoundary rightBoundary;

	@Nullable
	private final T rightValue;

	/**
	 * Creates a new interval.
	 *
	 * @param aLeftBoundary left boundary
	 * @param aLeftValue left value (may be {@code null} if the boundary ignores its value)
	 * @param aRightBoundary right boundary
	 * @param aRightValue right value (may be {@code null} if the boundary ignores its value)
	 */
	public AIcInterval(@Nonnull final AInIntervalBoundary aLeftBoundary,
			@Nullable final T aLeftValue,
			@Nonnull final AInIntervalBoundary aRightBoundary,
			@Nullable final T aRightValue) {
		this(aLeftBoundary, aLeftValue, aRightBoundary, aRightValue, false);
	}

	/**
	 * Creates a new interval.
	 *
	 * <p>
	 * This constructor exists primarily for subclasses that need to defer validation
	 * (e.g., when ordering must be evaluated using a custom comparator).
	 * </p>
	 *
	 * @param aLeftBoundary left boundary
	 * @param aLeftValue left value (may be {@code null} if the boundary ignores its value)
	 * @param aRightBoundary right boundary
	 * @param aRightValue right value (may be {@code null} if the boundary ignores its value)
	 * @param aSkipValidation when {@code true}, the constructor does not validate the interval
	 */
	protected AIcInterval(@Nonnull final AInIntervalBoundary aLeftBoundary,
			@Nullable final T aLeftValue,
			@Nonnull final AInIntervalBoundary aRightBoundary,
			@Nullable final T aRightValue,
			final boolean aSkipValidation) {
		leftBoundary = Objects.requireNonNull(aLeftBoundary, "Left boundary must not be null");
		rightBoundary = Objects.requireNonNull(aRightBoundary, "Right boundary must not be null");
		leftValue = aLeftValue;
		rightValue = aRightValue;

		if (!aSkipValidation) {
			AIsIntervalUtils.validateInterval(this);
		}
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public AInIntervalBoundary getLeftBoundary() {
		return leftBoundary;
	}

	/** {@inheritDoc} */
	@Override
	public T getLeftValue() {
		return leftValue;
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public AInIntervalBoundary getRightBoundary() {
		return rightBoundary;
	}

	/** {@inheritDoc} */
	@Override
	public T getRightValue() {
		return rightValue;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object aOther) {
		if (this == aOther) {
			return true;
		}
		if (!(aOther instanceof AIiInterval)) {
			return false;
		}
		AIiInterval<?> locOther = (AIiInterval<?>) aOther;
		return leftBoundary == locOther.getLeftBoundary()
				&& rightBoundary == locOther.getRightBoundary()
				&& Objects.equals(leftValue, locOther.getLeftValue())
				&& Objects.equals(rightValue, locOther.getRightValue());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(leftBoundary, leftValue, rightBoundary, rightValue);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return AIsIntervalUtils.toStringRepresentation(this);
	}
}
