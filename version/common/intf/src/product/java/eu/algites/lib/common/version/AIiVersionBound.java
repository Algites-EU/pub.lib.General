package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

/** Boundary of a portable version requirement. */
public interface AIiVersionBound {
	@Nonnull
	String versionText();

	boolean inclusive();
}
