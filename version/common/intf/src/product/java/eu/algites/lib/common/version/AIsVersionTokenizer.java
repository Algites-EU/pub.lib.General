package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIsVersionTokenizer}
 * </p>
 * <p>
 * Description: Tokenizes version text into {@link AIcVersionToken} instances.
 * </p>
 * <p>
 * Tokenization strategy:
 * </p>
 * <ul>
 *   <li>Alphanumeric runs {@code [A-Za-z0-9]+} become {@link AInVersionTokenType#ALPHANUMERIC}.</li>
 *   <li>Everything else becomes {@link AInVersionTokenType#SEPARATOR}.</li>
 * </ul>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIsVersionTokenizer {

	private AIsVersionTokenizer() {
		/* utility class */
	}

	/**
	 * Tokenizes version text into {@link AIcVersionToken} instances.
	 * @param aVersionText version text to tokenize
	 * @return list of tokens
	 */
	@Nonnull
	public static List<AIcVersionToken> tokenize(@Nonnull final String aVersionText) {
		Objects.requireNonNull(aVersionText, "Version text must not be null");

		String locTrimmed = aVersionText.trim();
		if (locTrimmed.isEmpty()) {
			return Collections.emptyList();
		}

		List<AIcVersionToken> locTokens = new ArrayList<>();

		int locStart = 0;
		AInVersionTokenType locCurrentType = tokenTypeOf(locTrimmed.charAt(0));

		for (int locIndex = 1; locIndex < locTrimmed.length(); locIndex++) {
			AInVersionTokenType locType = tokenTypeOf(locTrimmed.charAt(locIndex));
			if (locType != locCurrentType) {
				String locText = locTrimmed.substring(locStart, locIndex);
				locTokens.add(new AIcVersionToken(locCurrentType, locText));
				locStart = locIndex;
				locCurrentType = locType;
			}
		}

		String locText = locTrimmed.substring(locStart);
		locTokens.add(new AIcVersionToken(locCurrentType, locText));

		return Collections.unmodifiableList(locTokens);
	}

	@Nonnull
	private static AInVersionTokenType tokenTypeOf(final char aChar) {
		boolean locIsAlnum = (aChar >= '0' && aChar <= '9')
				|| (aChar >= 'a' && aChar <= 'z')
				|| (aChar >= 'A' && aChar <= 'Z');

		return locIsAlnum ? AInVersionTokenType.ALPHANUMERIC : AInVersionTokenType.SEPARATOR;
	}
}
