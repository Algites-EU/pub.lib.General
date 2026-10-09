package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.util.Objects;

/**
 * <p>
 * Title: {@link AIrVersionSchemeTextParts}
 * </p>
 * <p>
 * Description: Split result of a version string into the precedence-relevant part and the build-identification part.
 * </p>
 *
 * @param versionText the precedence-relevant version text (never null)
 * @param buildText the build-identification text (never null, may be empty)
 *
 * @author linhart1
 * @date 28.01.26
 */
public record AIrVersionSchemeTextParts(
		@Nonnull String versionText,
		@Nonnull String buildText
) implements AIiVersionSchemeTextParts {

	public AIrVersionSchemeTextParts {
		Objects.requireNonNull(versionText, "versionText must not be null");
		Objects.requireNonNull(buildText, "buildText must not be null");
	}

	@Override
	public boolean hasBuild() {
		return !buildText.isEmpty();
	}
}
