package eu.algites.lib.common.object.rendering;

/**
 * <p>
 * Title: {@link AIiRenderingOutputPurposeResolver}
 * </p>
 * <p>
 * Description: Resolver interface for the rendering output purpose resolution
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
public interface AIiRenderingOutputPurposeResolver {
	/**
	 * Resolves the given purpose
	 * @return the resolved purpose
	 */
	AIiRenderingOutputPurpose resolvePurpose();
}
