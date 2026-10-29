package eu.algites.lib.common.object.rendering;

import java.util.Locale;

/**
 * <p>
 * Title: {@link AIiRenderingOutputLocaleResolver}
 * </p>
 * <p>
 * Description: Resolver interface for the rendering output locale resolution
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 6:17
 */
public interface AIiRenderingOutputLocaleResolver {
	/**
	 * Resolves the given locale
	 * @return the resolved locale
	 */
	Locale resolveLocale();
}
