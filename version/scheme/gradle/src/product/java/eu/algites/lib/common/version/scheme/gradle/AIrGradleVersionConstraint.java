package eu.algites.lib.common.version.scheme.gradle;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * Technology-neutral representation of Gradle rich version-constraint fields.
 */
public record AIrGradleVersionConstraint(
		@Nullable String require,
		@Nullable String strictly,
		@Nullable String prefer,
		List<String> reject
) {
	public AIrGradleVersionConstraint {
		reject = reject == null ? List.of() : List.copyOf(reject);
	}

	public static AIrGradleVersionConstraint require(final String aRequire) {
		return new AIrGradleVersionConstraint(aRequire, null, null, List.of());
	}
}
