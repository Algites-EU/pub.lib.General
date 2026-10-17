package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcVersionToken}
 * </p>
 * <p>
 * Description: Single token of a version string (alphanumeric or separator).
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcVersionToken {

	@Nonnull
	private final AInVersionTokenType tokenType;

	@Nonnull
	private final String text;

	/**
	 * Creates an individual parsed version token with its category and text.
	 * @param aTokenType token type supplied to the operation
	 * @param aText text supplied to the operation
	 */
	public AIcVersionToken(@Nonnull final AInVersionTokenType aTokenType, @Nonnull final String aText) {
		tokenType = Objects.requireNonNull(aTokenType, "Token type must not be null");
		text = Objects.requireNonNull(aText, "Token text must not be null");

		if (text.isEmpty()) {
			throw new IllegalArgumentException("Token text must not be empty");
		}
	}

	/**
	 * Returns the token type.
	 * @return the token type
	 */
	@Nonnull
	public AInVersionTokenType getTokenType() {
		return tokenType;
	}

	/**
	 * Returns the text.
	 * @return the text
	 */
	@Nonnull
	public String getText() {
		return text;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return tokenType + ":" + text;
	}
}
