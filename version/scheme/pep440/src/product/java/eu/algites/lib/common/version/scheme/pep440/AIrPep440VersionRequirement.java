package eu.algites.lib.common.version.scheme.pep440;

import jakarta.annotation.Nonnull;
import java.util.Objects;

/** Immutable PEP 440 version-specifier expression. */
public record AIrPep440VersionRequirement(@Nonnull String text) {
	public AIrPep440VersionRequirement {
		Objects.requireNonNull(text, "text must not be null");
		if (text.isBlank()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		text = text.trim();
	}
}
