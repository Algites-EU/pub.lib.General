package eu.algites.lib.common.interval;

import jakarta.annotation.Nonnull;

/**
 * <p>
 * Title: {@link AIiInterval}
 * </p>
 * <p>
 * Description: General interval definition
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 26.01.26 15:44
 */
public interface AIiInterval<T extends Comparable> {

	/**
	 * Left boundary of the interval
	 * @return left boundary
	 */
	@Nonnull
	AInIntervalBoundary getLeftBoundary();

	/**
	 * Left value of the interval
	 * @return left value. The left value may be unspecified
	 *    in the case {@link #getLeftBoundary()}
	 *    returns true for {@link AInIntervalBoundary#isBoundaryValueIgnored()}
	 */
	T getLeftValue();

	/**
	 * Right boundary of the interval
	 * @return right boundary
	 */
	@Nonnull
	AInIntervalBoundary getRightBoundary();

	/**
	 * Right value of the interval
	 * @return right value. The right value may be unspecified
	 *    in the case {@link #getRightBoundary()}
	 *    returns true for {@link AInIntervalBoundary#isBoundaryValueIgnored()}
	 */
	T getRightValue();
}
