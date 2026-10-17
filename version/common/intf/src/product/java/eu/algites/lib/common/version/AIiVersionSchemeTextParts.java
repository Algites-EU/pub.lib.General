package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AIiVersionSchemeTextParts}
 * </p>
 * <p>
 * Description: TODO: Add description
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 29.01.26 6:56
 */
public interface AIiVersionSchemeTextParts {
	/**
	 * Checks whether build is enabled or present.
	 * @return the has build
	 */
	boolean hasBuild();

	/**
	 * Returns the textual representation of the version or bound.
	 * @return the result of the operation
	 */
	String versionText();

	/**
	 * Returns the optional build component of the version.
	 * @return the result of the operation
	 */
	String buildText();
}
