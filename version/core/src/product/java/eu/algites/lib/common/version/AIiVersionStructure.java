package eu.algites.lib.common.version;

import eu.algites.lib.common.enums.AIiEnumItem;
import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * <p>
 * Title: {@link AIiVersionStructure}
 * </p>
 * <p>
 * Description: Structural interpretation of version strings for a given {@link AIiVersionScheme}.
 * </p>
 * <p>
 * A structure defines where the build-identification part is located (before/after the version),
 * how it is delimited, and whether it participates in precedence comparison.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public interface AIiVersionStructure extends AIiEnumItem {

	/**
	 * @return true if the precedence-relevant version part is placed before the build-identification part
	 */
	default boolean versionBeforeBuild() {
		return true;
	}

	/**
	 * Build delimiter separating the precedence-relevant version part and the build-identification part.
	 * <p>
	 * An empty delimiter means that the scheme does not use a dedicated build-identification section.
	 * </p>
	 *
	 * @return build delimiter or empty string
	 */
	@Nonnull
	default String buildDelimiter() {
		return "";
	}

	/**
	 * @return policy defining whether and how the build-identification part participates in comparison
	 */
	@Nonnull
	default AInVersionBuildComparisonPolicy buildComparisonPolicy() {
		return AInVersionBuildComparisonPolicy.IGNORE;
	}

	/**
	 * Splits the provided version text into its precedence-relevant part and its build-identification part.
	 * <p>
	 * The split is based on {@link #buildDelimiter()} and {@link #versionBeforeBuild()}.
	 * </p>
	 *
	 * @param aVersionText raw version text
	 * @return split parts
	 */
	@Nonnull
	default AIiVersionSchemeTextParts splitVersionAndBuildText(@Nonnull final String aVersionText) {
		Objects.requireNonNull(aVersionText, "Version text must not be null");

		String locDelimiter = buildDelimiter();
		if (locDelimiter.isEmpty()) {
			return new AIrVersionSchemeTextParts(aVersionText, "");
		}

		int locIndex = aVersionText.indexOf(locDelimiter);
		if (locIndex < 0) {
			return new AIrVersionSchemeTextParts(aVersionText, "");
		}

		String locFirst = aVersionText.substring(0, locIndex);
		String locSecond = aVersionText.substring(locIndex + locDelimiter.length());

		if (versionBeforeBuild()) {
			return new AIrVersionSchemeTextParts(locFirst, locSecond);
		}
		return new AIrVersionSchemeTextParts(locSecond, locFirst);
	}


}
