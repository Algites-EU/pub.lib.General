package eu.algites.lib.common.object.rendering;

/**
 * <p>
 * Title: {@link AInRenderingOutputBuiltinPurpose}
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
 * @date 12.01.26 4:48
 */
public enum AInRenderingOutputBuiltinPurpose implements AIiRenderingOutputPurpose {
	/**
	 * Some general behavior, predefined if nothing else specified
	 */
	DEFAULT,

	/**
	 * Output generated for user purpose
	 */
	USER,

	/**
	 * Output generated for system purpose. So, naturally probably some extension
	 * of {@link #DEFAULT} value
	 */
	SYSTEM,

	/**
	 * Output generated for debugging purpose
	 * (so, naturally rather extension of the {@link #SYSTEM} value)
	 */
	DEBUG;

	/** {@inheritDoc} */
	@Override
	public String code() {
		return name();
	}

}
