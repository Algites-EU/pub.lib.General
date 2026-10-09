package eu.algites.lib.common.version;

import eu.algites.lib.common.interval.AInIntervalBoundary;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Title: {@link AItcVersionIntervalTest}
 * </p>
 * <p>
 * Description: TestNG tests for {@link AIcVersionInterval}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public class AItcVersionIntervalTest {

	@Test
	public void testContainsUsingSemverScheme() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_DEFAULT;

		AIcVersionInterval locInterval = new AIcVersionInterval(
				AInIntervalBoundary.CLOSED,
				new AIcVersion("1.0.0"),
				AInIntervalBoundary.OPEN,
				new AIcVersion("2.0.0"),
				locScheme
		);

		Assert.assertTrue(
				locInterval.contains(new AIcVersion("1.0.0"), locScheme),
				"Left closed boundary must include the boundary value"
		);

		Assert.assertTrue(
				locInterval.contains(new AIcVersion("1.5.0"), locScheme),
				"Mid value must be contained"
		);

		Assert.assertFalse(
				locInterval.contains(new AIcVersion("2.0.0"), locScheme),
				"Right open boundary must exclude the boundary value"
		);

		Assert.assertFalse(
				locInterval.contains(new AIcVersion("0.9.9"), locScheme),
				"Value below the interval must not be contained"
		);
	}

	@Test
	public void testOverlapsAndIntersectUsingSemverScheme() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_DEFAULT;

		AIcVersionInterval locLeft = new AIcVersionInterval(
				AInIntervalBoundary.CLOSED,
				new AIcVersion("1.0.0"),
				AInIntervalBoundary.OPEN,
				new AIcVersion("2.0.0"),
				locScheme
		);

		AIcVersionInterval locRight = new AIcVersionInterval(
				AInIntervalBoundary.OPEN,
				new AIcVersion("1.5.0"),
				AInIntervalBoundary.CLOSED,
				new AIcVersion("3.0.0"),
				locScheme
		);

		Assert.assertTrue(
				locLeft.overlaps(locRight, locScheme),
				"Intervals must overlap"
		);

		AIcVersionInterval locIntersection = locLeft.intersect(locRight, locScheme);

		Assert.assertEquals(
				locIntersection.getLeftBoundary(),
				AInIntervalBoundary.OPEN,
				"Intersection left boundary must be open when any contributing left boundary is open at the same value"
		);

		Assert.assertEquals(
				locIntersection.getLeftValue().getOriginalText(),
				"1.5.0",
				"Intersection left value must be the maximum of left endpoints"
		);

		Assert.assertEquals(
				locIntersection.getRightBoundary(),
				AInIntervalBoundary.OPEN,
				"Intersection right boundary must be open when any contributing right boundary is open at the same value"
		);

		Assert.assertEquals(
				locIntersection.getRightValue().getOriginalText(),
				"2.0.0",
				"Intersection right value must be the minimum of right endpoints"
		);
	}

	@Test
	public void testTryIntersectReturnsNullWhenTouchingAtOpenPoint() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_DEFAULT;

		AIcVersionInterval locLeft = new AIcVersionInterval(
				AInIntervalBoundary.CLOSED,
				new AIcVersion("1.0.0"),
				AInIntervalBoundary.OPEN,
				new AIcVersion("1.1.0"),
				locScheme
		);

		AIcVersionInterval locRight = new AIcVersionInterval(
				AInIntervalBoundary.CLOSED,
				new AIcVersion("1.1.0"),
				AInIntervalBoundary.CLOSED,
				new AIcVersion("2.0.0"),
				locScheme
		);

		Assert.assertNull(
				locLeft.tryIntersect(locRight, locScheme),
				"Intervals that only touch at an excluded point must have no intersection"
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testConstructorRejectsInvalidIntervalUsingSchemeOrdering() {
		AIiVersionScheme locScheme = AInBuiltinVersionScheme.SEMVER_DEFAULT;

		new AIcVersionInterval(
				AInIntervalBoundary.CLOSED,
				new AIcVersion("2.0.0"),
				AInIntervalBoundary.CLOSED,
				new AIcVersion("1.0.0"),
				locScheme
		);
	}
}
