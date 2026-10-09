package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcDefaultVersionFormatter}
 * </p>
 * <p>
 * Description: Default formatter using {@link AIiVersionStructure} and {@link AIiVersionFormat}.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public final class AIcDefaultVersionFormatter implements AIiVersionFormatter, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	public static final AIcDefaultVersionFormatter INSTANCE = new AIcDefaultVersionFormatter();

	private AIcDefaultVersionFormatter() {
	}

	@Override
	@Nonnull
	public String formatVersionText(@Nonnull final AIcVersion aVersion, @Nonnull final AIiVersionScheme aScheme) {
		Objects.requireNonNull(aVersion, "Version must not be null");
		Objects.requireNonNull(aScheme, "Scheme must not be null");

		AIiVersionStructure locStructure = aScheme.versionStructure();
		AIiVersionFormat locFormat = aScheme.versionFormat();

		AIiVersionSchemeTextParts locParts = aScheme.versionStructure().splitVersionAndBuildText(aVersion.getOriginalText());

		String locVersionPart = locParts.versionText();
		String locBuildPart = locParts.buildText();

		AInVersionBuildFormatPolicy locBuildPolicy = locFormat.buildFormatPolicy();
		if (locBuildPart.isEmpty() || locBuildPolicy == AInVersionBuildFormatPolicy.OMIT) {
			return locVersionPart;
		}

		if (locBuildPolicy == AInVersionBuildFormatPolicy.EMIT) {
			String locDelimiter = locStructure.buildDelimiter();
			if (locDelimiter.isEmpty()) {
				throw new IllegalStateException("Build format policy EMIT requires a non-empty build delimiter");
			}
			if (locStructure.versionBeforeBuild()) {
				return locVersionPart + locDelimiter + locBuildPart;
			}
			return locBuildPart + locDelimiter + locVersionPart;
		}

		if (locBuildPolicy == AInVersionBuildFormatPolicy.MAP_TO_QUALIFIER) {
			return mapBuildToQualifier(locVersionPart, locBuildPart, locFormat);
		}

		throw new IllegalStateException("Unsupported build format policy: " + locBuildPolicy);
	}

	@Nonnull
	private static String mapBuildToQualifier(
			@Nonnull final String aVersionPart,
			@Nonnull final String aBuildPart,
			@Nonnull final AIiVersionFormat aFormat
	) {
		String locQualifierDelimiter = aFormat.qualifierDelimiter();
		String locTokenDelimiter = aFormat.qualifierTokenDelimiter();
		String locPrefix = aFormat.mappedBuildPrefix();

		String locMapped = locPrefix + locTokenDelimiter + aBuildPart;

		int locQualifierIndex = aVersionPart.indexOf(locQualifierDelimiter);
		if (locQualifierIndex < 0) {
			return aVersionPart + locQualifierDelimiter + locMapped;
		}
		return aVersionPart + locTokenDelimiter + locMapped;
	}
}
