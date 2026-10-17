package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

/** Boundary of a portable version requirement. */
public interface AIiVersionBound {
	/**
	 * Returns the textual representation of the version or bound.
	 * @return the result of the operation
	 */
	@Nonnull
	String versionText();

	/**
	 * Reports whether the version bound includes its endpoint.
	 * @return the result of the operation
	 */
	boolean inclusive();
}
