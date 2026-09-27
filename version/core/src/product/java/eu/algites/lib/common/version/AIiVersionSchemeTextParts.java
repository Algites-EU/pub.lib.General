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
	boolean hasBuild();

	String versionText();

	String buildText();
}
