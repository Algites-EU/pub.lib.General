package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/** Portable version requirement independent of a concrete build ecosystem. */
public interface AIiVersionRequirement {
	/**
	 * Returns the exact required version text, if available.
	 * @return the result of the operation
	 */
	@Nullable
	String exactVersionText();

	/**
	 * Returns the minimum version bound, if present.
	 * @return the result of the operation
	 */
	@Nullable
	AIiVersionBound minimum();

	/**
	 * Returns the maximum version bound, if present.
	 * @return the result of the operation
	 */
	@Nullable
	AIiVersionBound maximum();

	/**
	 * Indicates whether the maximum version bound is exclusive.
	 * @return the result of the operation
	 */
	@Nullable
	Boolean maximumStrict();

	/**
	 * Reports whether the normalized upper bound excludes its endpoint.
	 * @return the result of the operation
	 */
	default boolean effectiveMaximumStrict() {
		return maximum() != null && (maximumStrict() == null || maximumStrict());
	}

	/**
	 * Returns versions explicitly excluded from the requirement.
	 * @return the result of the operation
	 */
	@Nonnull
	List<String> excludedVersionTexts();

	/**
	 * Returns the preferred version text, where specified.
	 * @return the result of the operation
	 */
	@Nullable
	String preferredVersionText();
}
