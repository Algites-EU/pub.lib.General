package eu.algites.lib.common.version.scheme.gradle;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Tests for {@link AIrGradleVersionConstraint}.
 */
public class AItcGradleVersionConstraintTest {

	@Test
	public void testRichConstraintStoresAllFields() {
		AIrGradleVersionConstraint locConstraint = new AIrGradleVersionConstraint(
				"1.5", "[1.2,2.0[", "1.8", List.of("1.6", "1.7")
		);
		Assert.assertEquals(locConstraint.prefer(), "1.8");
		Assert.assertEquals(locConstraint.reject().size(), 2);
	}
}
