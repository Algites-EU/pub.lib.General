package eu.algites.lib.common.version.scheme.gradle;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Technology-neutral representation of Gradle rich version-constraint fields.
 * @param require required version constraint, when specified
 * @param strictly strict version requirement, when specified
 * @param prefer preferred version, when specified
 * @param reject versions excluded by this requirement
 */
public record AIrGradleVersionConstraint(
		@Nullable String require,
		@Nullable String strictly,
		@Nullable String prefer,
		List<String> reject
) {
	/**
	 * Normalizes a Gradle rich version constraint, retaining its exclusion list.
	 * @param require require supplied to the operation
	 * @param strictly strictly supplied to the operation
	 * @param prefer prefer supplied to the operation
	 * @param reject reject supplied to the operation
	 */
	public AIrGradleVersionConstraint {
		reject = reject == null ? List.of() : List.copyOf(reject);
	}

	/**
	 * Creates a Gradle requirement with the supplied mandatory version constraint.
	 * @param aRequire require supplied to the operation
	 * @return the result of the operation
	 */
	public static AIrGradleVersionConstraint require(final String aRequire) {
		return new AIrGradleVersionConstraint(aRequire, null, null, List.of());
	}
}
