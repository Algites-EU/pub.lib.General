package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AInVersionTokenType}
 * </p>
 * <p>
 * Description: Token types produced by {@link AIsVersionTokenizer}.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public enum AInVersionTokenType {

	/**
	 * Alphanumeric token: a continuous sequence of {@code [A-Za-z0-9]+}.
	 */
	ALPHANUMERIC,

	/**
	 * Separator token: a continuous sequence of non-alphanumeric characters.
	 */
	SEPARATOR
}
