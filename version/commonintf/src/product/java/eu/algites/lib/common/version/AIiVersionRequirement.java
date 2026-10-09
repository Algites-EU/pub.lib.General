package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/** Portable version requirement independent of a concrete build ecosystem. */
public interface AIiVersionRequirement {
	@Nullable
	String exactVersionText();

	@Nullable
	AIiVersionBound minimum();

	@Nullable
	AIiVersionBound maximum();

	@Nullable
	Boolean maximumStrict();

	default boolean effectiveMaximumStrict() {
		return maximum() != null && (maximumStrict() == null || maximumStrict());
	}

	@Nonnull
	List<String> excludedVersionTexts();

	@Nullable
	String preferredVersionText();
}
