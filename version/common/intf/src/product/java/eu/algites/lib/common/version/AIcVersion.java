package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcVersion}
 * </p>
 * <p>
 * Description: Immutable version representation backed by tokenization.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public class AIcVersion implements Comparable<AIcVersion>, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * Default handling purpose used by {@link #compareTo(AIcVersion)}.
	 */
	@Nonnull
	public static final AIiVersionScheme DEFAULT_HANDLING_MODE = AInBuiltinVersionScheme.MAVEN_DEFAULT;

	@Nonnull
	private final String originalText;

	@Nonnull
	private final List<AIcVersionToken> tokens;

	/**
	 * Creates a version instance with the supplied configuration.
	 * @param aOriginalText original unparsed version text
	 */
	public AIcVersion(@Nonnull final String aOriginalText) {
		originalText = Objects.requireNonNull(aOriginalText, "Original text must not be null");
		tokens = AIsVersionTokenizer.tokenize(originalText);
	}

	/**
	 * Returns the original text.
	 * @return the original text
	 */
	@Nonnull
	public String getOriginalText() {
		return originalText;
	}

	/**
	 * Returns the tokens.
	 * @return the tokens
	 */
	@Nonnull
	public List<AIcVersionToken> getTokens() {
		return tokens;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(@Nonnull final AIcVersion aOther) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		return compareTo(aOther, getHandlingMode());
	}

	/**
	 * Compares this instance with the supplied value.
	 * @param aOther other supplied to the operation
	 * @param aHandlingMode handling mode supplied to the operation
	 * @return the result of the operation
	 */
	public int compareTo(@Nonnull final AIcVersion aOther, @Nonnull final AIiVersionScheme aHandlingMode) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		Objects.requireNonNull(aHandlingMode, "Handling purpose must not be null");
		return AIsVersionComparator.compare(this, aOther, aHandlingMode);
	}

	/**
	 * Compares this instance with the supplied value.
	 * @param aOther other supplied to the operation
	 * @param aComparator comparator supplied to the operation
	 * @return the result of the operation
	 */
	public int compareTo(@Nonnull final AIcVersion aOther, @Nonnull final AIiVersionComparator aComparator) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		Objects.requireNonNull(aComparator, "Comparator must not be null");
		return AIsVersionComparator.compare(this, aOther, aComparator);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return originalText;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object aOther) {
		if (this == aOther) {
			return true;
		}
		if (!(aOther instanceof AIcVersion)) {
			return false;
		}
		AIcVersion locOther = (AIcVersion) aOther;
		return originalText.equals(locOther.originalText);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return originalText.hashCode();
	}

	/**
	 * Returns the handling mode.
	 * @return the handling mode
	 */
	protected AIiVersionScheme getHandlingMode() {
		return DEFAULT_HANDLING_MODE;
	}
}
