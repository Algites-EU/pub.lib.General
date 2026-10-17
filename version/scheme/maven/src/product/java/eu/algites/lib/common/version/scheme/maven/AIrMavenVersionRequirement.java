package eu.algites.lib.common.version.scheme.maven;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * Immutable Maven-native version requirement text.
 * @param text version constraint or requirement text
 */
public record AIrMavenVersionRequirement(@Nonnull String text) {

	/**
	 * Creates a maven version requirement instance with the supplied configuration.
	 * @param text text supplied to the operation
	 */
	public AIrMavenVersionRequirement {
		Objects.requireNonNull(text, "text must not be null");
		if (text.isBlank()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		text = text.trim();
	}
}
