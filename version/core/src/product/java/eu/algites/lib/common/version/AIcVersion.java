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

	public AIcVersion(@Nonnull final String aOriginalText) {
		originalText = Objects.requireNonNull(aOriginalText, "Original text must not be null");
		tokens = AIsVersionTokenizer.tokenize(originalText);
	}

	@Nonnull
	public String getOriginalText() {
		return originalText;
	}

	@Nonnull
	public List<AIcVersionToken> getTokens() {
		return tokens;
	}

	@Override
	public int compareTo(@Nonnull final AIcVersion aOther) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		return compareTo(aOther, getHandlingMode());
	}

	public int compareTo(@Nonnull final AIcVersion aOther, @Nonnull final AIiVersionScheme aHandlingMode) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		Objects.requireNonNull(aHandlingMode, "Handling purpose must not be null");
		return AIsVersionComparator.compare(this, aOther, aHandlingMode);
	}

	public int compareTo(@Nonnull final AIcVersion aOther, @Nonnull final AIiVersionComparator aComparator) {
		Objects.requireNonNull(aOther, "Other version must not be null");
		Objects.requireNonNull(aComparator, "Comparator must not be null");
		return AIsVersionComparator.compare(this, aOther, aComparator);
	}

	@Override
	public String toString() {
		return originalText;
	}

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

	@Override
	public int hashCode() {
		return originalText.hashCode();
	}

	protected AIiVersionScheme getHandlingMode() {
		return DEFAULT_HANDLING_MODE;
	}
}
