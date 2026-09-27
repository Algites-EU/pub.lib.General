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

	public AIcVersionToken(@Nonnull final AInVersionTokenType aTokenType, @Nonnull final String aText) {
		tokenType = Objects.requireNonNull(aTokenType, "Token type must not be null");
		text = Objects.requireNonNull(aText, "Token text must not be null");

		if (text.isEmpty()) {
			throw new IllegalArgumentException("Token text must not be empty");
		}
	}

	@Nonnull
	public AInVersionTokenType getTokenType() {
		return tokenType;
	}

	@Nonnull
	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return tokenType + ":" + text;
	}
}
