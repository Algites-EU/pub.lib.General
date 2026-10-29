package eu.algites.lib.common.object.rendering;

/**
 * <p>
 * Title: {@link AIcDefaultRenderingOutputPurposeResolver}
 * </p>
 * <p>
 * Description: Default resolver if not specified in the annotation otherwise
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 12.01.26 6:19
 */
public class AIcDefaultRenderingOutputPurposeResolver implements AIiRenderingOutputPurposeResolver {
	/** {@inheritDoc} */
	@Override
	public AIiRenderingOutputPurpose resolvePurpose() {
		return AIsRenderingOutputUtils.usedRenderingOutputPurpose();
	}

}