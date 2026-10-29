package eu.algites.lib.common.object.rendering;

/**
 * <p>
 * Title: {@link AInRenderingOutputBuiltinFormat}
 * </p>
 * <p>
 * Description: Builtin format of the rendering.
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
public enum AInRenderingOutputBuiltinFormat implements AIiRenderingOutputFormat {

	/**
	 * Some general behavior, predefined if nothing else specified
	 */
	DEFAULT,

	/**
	 * Output generated in plain text, so natural {@link #DEFAULT} rendering
	 */
	PLAIN_TEXT,

	/**
	 * Output generated in XML format
	 */
	XML,

	/**
	 * Output generated in JSON Format
	 */
	JSON;

	/** {@inheritDoc} */
	@Override
	public String code() {
		return name();
	}

}
