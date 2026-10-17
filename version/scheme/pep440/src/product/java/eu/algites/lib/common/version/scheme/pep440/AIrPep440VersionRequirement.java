package eu.algites.lib.common.version.scheme.pep440;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/**
 * Immutable PEP 440 version-specifier expression.
 * @param text version constraint or requirement text
 */
public record AIrPep440VersionRequirement(@Nonnull String text) {
	/**
	 * Creates a pep440 version requirement instance with the supplied configuration.
	 * @param text text supplied to the operation
	 */
	public AIrPep440VersionRequirement {
		Objects.requireNonNull(text, "text must not be null");
		if (text.isBlank()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		text = text.trim();
	}
}
