package eu.algites.lib.common.interval;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Title: {@link AItsIntervalUtilsTest}
 * </p>
 * <p>
 * Description: TestNG tests for {@link AIsIntervalUtils}.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public class AItsIntervalUtilsTest {

	@Test
	public void testValidateIntervalAcceptsClosedRange() {
		AIcInterval<Integer> locInterval = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertTrue(AIsIntervalUtils.isSatisfiable(locInterval), "Closed range must be satisfiable");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testValidateIntervalRejectsNullLeftValueWhenNotIgnored() {
		new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				null,
				AInIntervalBoundary.CLOSED,
				3
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testValidateIntervalRejectsNullRightValueWhenNotIgnored() {
		new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testValidateIntervalRejectsLeftGreaterThanRight() {
		new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				5,
				AInIntervalBoundary.CLOSED,
				3
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testValidateIntervalRejectsOpenPointInterval() {
		new AIcInterval<>(
				AInIntervalBoundary.OPEN,
				1,
				AInIntervalBoundary.OPEN,
				1
		);
	}

	@Test
	public void testValidateIntervalAcceptsClosedPointInterval() {
		AIcInterval<Integer> locInterval = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				1
		);

		Assert.assertTrue(AIsIntervalUtils.contains(locInterval, 1), "Closed point interval must contain its single value");
	}

	@Test
	public void testIsLeftRightUnbounded() {
		AIcInterval<Integer> locLeftUnbounded = new AIcInterval<>(
				AInIntervalBoundary.UNBOUNDED,
				null,
				AInIntervalBoundary.CLOSED,
				3
		);

		AIcInterval<Integer> locRightUnbounded = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				3,
				AInIntervalBoundary.UNBOUNDED,
				null
		);

		Assert.assertTrue(AIsIntervalUtils.isLeftUnbounded(locLeftUnbounded), "Left boundary UNBOUNDED must be detected");
		Assert.assertFalse(AIsIntervalUtils.isRightUnbounded(locLeftUnbounded), "Right boundary CLOSED must not be detected as unbounded");

		Assert.assertFalse(AIsIntervalUtils.isLeftUnbounded(locRightUnbounded), "Left boundary CLOSED must not be detected as unbounded");
		Assert.assertTrue(AIsIntervalUtils.isRightUnbounded(locRightUnbounded), "Right boundary UNBOUNDED must be detected");
	}

	@Test
	public void testContainsRespectsOpenClosedBoundaries() {
		AIcInterval<Integer> locOpenOpen = new AIcInterval<>(
				AInIntervalBoundary.OPEN,
				1,
				AInIntervalBoundary.OPEN,
				3
		);

		Assert.assertFalse(AIsIntervalUtils.contains(locOpenOpen, 1), "(1,3) must not contain 1");
		Assert.assertTrue(AIsIntervalUtils.contains(locOpenOpen, 2), "(1,3) must contain 2");
		Assert.assertFalse(AIsIntervalUtils.contains(locOpenOpen, 3), "(1,3) must not contain 3");

		AIcInterval<Integer> locClosedClosed = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertTrue(AIsIntervalUtils.contains(locClosedClosed, 1), "[1,3] must contain 1");
		Assert.assertTrue(AIsIntervalUtils.contains(locClosedClosed, 3), "[1,3] must contain 3");
	}

	@Test
	public void testContainsWithUnboundedSides() {
		AIcInterval<Integer> locLeftUnbounded = new AIcInterval<>(
				AInIntervalBoundary.UNBOUNDED,
				null,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertTrue(AIsIntervalUtils.contains(locLeftUnbounded, -1000), "(*,3] must contain -1000");
		Assert.assertTrue(AIsIntervalUtils.contains(locLeftUnbounded, 3), "(*,3] must contain 3");
		Assert.assertFalse(AIsIntervalUtils.contains(locLeftUnbounded, 4), "(*,3] must not contain 4");

		AIcInterval<Integer> locRightUnbounded = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.UNBOUNDED,
				null
		);

		Assert.assertFalse(AIsIntervalUtils.contains(locRightUnbounded, 0), "[1,*) must not contain 0");
		Assert.assertTrue(AIsIntervalUtils.contains(locRightUnbounded, 1000), "[1,*) must contain 1000");
	}

	@Test
	public void testTryIntersectAndOverlapsForTouchingClosedIntervals() {
		AIcInterval<Integer> locLeft = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		AIcInterval<Integer> locRight = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				3,
				AInIntervalBoundary.CLOSED,
				5
		);

		Assert.assertTrue(AIsIntervalUtils.overlaps(locLeft, locRight), "Touching closed intervals must overlap");

		AIcInterval<Integer> locIntersection = AIsIntervalUtils.tryIntersect(locLeft, locRight);
		Assert.assertNotNull(locIntersection, "Intersection must exist for touching closed intervals");
		Assert.assertEquals(locIntersection.getLeftBoundary(), AInIntervalBoundary.CLOSED, "Intersection left boundary must be closed");
		Assert.assertEquals(locIntersection.getLeftValue(), Integer.valueOf(3), "Intersection left value must be 3");
		Assert.assertEquals(locIntersection.getRightBoundary(), AInIntervalBoundary.CLOSED, "Intersection right boundary must be closed");
		Assert.assertEquals(locIntersection.getRightValue(), Integer.valueOf(3), "Intersection right value must be 3");
	}

	@Test
	public void testTryIntersectReturnsNullForOpenTouchingIntervals() {
		AIcInterval<Integer> locLeft = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.OPEN,
				3
		);

		AIcInterval<Integer> locRight = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				3,
				AInIntervalBoundary.CLOSED,
				5
		);

		Assert.assertFalse(AIsIntervalUtils.overlaps(locLeft, locRight), "Open touching must not overlap");
		Assert.assertNull(AIsIntervalUtils.tryIntersect(locLeft, locRight), "Intersection must be null for open touching");
		Assert.assertTrue(AIsIntervalUtils.isStrictlyBefore(locLeft, locRight), "Open touching must be strictly before");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testIntersectThrowsWhenNoOverlap() {
		AIcInterval<Integer> locLeft = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.OPEN,
				3
		);

		AIcInterval<Integer> locRight = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				3,
				AInIntervalBoundary.CLOSED,
				5
		);

		AIsIntervalUtils.intersect(locLeft, locRight);
	}

	@Test
	public void testCompareOrdersByLeftThenRightEndpoints() {
		AIcInterval<Integer> locUnboundedLeft = new AIcInterval<>(
				AInIntervalBoundary.UNBOUNDED,
				null,
				AInIntervalBoundary.CLOSED,
				3
		);

		AIcInterval<Integer> locBoundedLeft = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertTrue(AIsIntervalUtils.compare(locUnboundedLeft, locBoundedLeft) < 0, "Unbounded left must sort before bounded left");

		AIcInterval<Integer> locClosedAtOne = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				2
		);

		AIcInterval<Integer> locOpenAtOne = new AIcInterval<>(
				AInIntervalBoundary.OPEN,
				1,
				AInIntervalBoundary.CLOSED,
				2
		);

		Assert.assertTrue(AIsIntervalUtils.compare(locClosedAtOne, locOpenAtOne) < 0, "Closed left must sort before open left at same value");

		AIcInterval<Integer> locRightClosed = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		AIcInterval<Integer> locRightOpen = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.OPEN,
				3
		);

		Assert.assertTrue(AIsIntervalUtils.compare(locRightClosed, locRightOpen) > 0, "Closed right must sort after open right at same value");
	}

	@Test
	public void testToStringRepresentationUsesBoundarySymbols() {
		AIcInterval<Integer> locClosed = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertEquals(AIsIntervalUtils.toStringRepresentation(locClosed), "[1, 3]", "Closed interval string representation must match");

		AIcInterval<Integer> locOpen = new AIcInterval<>(
				AInIntervalBoundary.OPEN,
				1,
				AInIntervalBoundary.OPEN,
				3
		);

		Assert.assertEquals(AIsIntervalUtils.toStringRepresentation(locOpen), "(1, 3)", "Open interval string representation must match");

		AIcInterval<Integer> locLeftUnbounded = new AIcInterval<>(
				AInIntervalBoundary.UNBOUNDED,
				null,
				AInIntervalBoundary.CLOSED,
				3
		);

		Assert.assertEquals(AIsIntervalUtils.toStringRepresentation(locLeftUnbounded), "(*, 3]", "Unbounded-left string representation must match");

		AIcInterval<Integer> locRightUnbounded = new AIcInterval<>(
				AInIntervalBoundary.CLOSED,
				1,
				AInIntervalBoundary.UNBOUNDED,
				null
		);

		Assert.assertEquals(AIsIntervalUtils.toStringRepresentation(locRightUnbounded), "[1, *)", "Unbounded-right string representation must match");
	}
}
