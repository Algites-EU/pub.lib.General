package eu.algites.lib.common.object.rendering;

/**
 * <p>
 * Title: {@link AIiRenderingOutputFormatResolver}
 * </p>
 * <p>
 * Description: Resolver interface for the rendering output format resolution
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
public interface AIiRenderingOutputFormatResolver {
	/**
	 * Resolves the given format
	 * @return the resolved format
	 */
	AIiRenderingOutputFormat resolveFormat();
}
