package eu.algites.lib.common.version;

import jakarta.annotation.Nonnull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * Title: {@link AIcDefaultVersionCodec}
 * </p>
 * <p>
 * Description: Default codec using {@link AIcDefaultVersionFormatter}.
 * </p>
 * <p>
 * The codec is intentionally lightweight: it mainly trims input and delegates formatting to the formatter.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public final class AIcDefaultVersionCodec implements AIiVersionCodec, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Nonnull
	public static final AIcDefaultVersionCodec INSTANCE = new AIcDefaultVersionCodec(AIcDefaultVersionFormatter.INSTANCE);

	@Nonnull
	private final AIiVersionFormatter versionFormatter;

	public AIcDefaultVersionCodec(@Nonnull final AIiVersionFormatter aVersionFormatter) {
		versionFormatter = Objects.requireNonNull(aVersionFormatter, "Version formatter must not be null");
	}

	@Override
	@Nonnull
	public AIcVersion parseVersion(@Nonnull final String aVersionText, @Nonnull final AIiVersionScheme aScheme) {
		Objects.requireNonNull(aVersionText, "Version text must not be null");
		Objects.requireNonNull(aScheme, "Scheme must not be null");

		String locText = aVersionText.trim();
		return new AIcVersion(locText);
	}

	@Override
	@Nonnull
	public String formatVersionText(@Nonnull final AIcVersion aVersion, @Nonnull final AIiVersionScheme aScheme) {
		Objects.requireNonNull(aVersion, "Version must not be null");
		Objects.requireNonNull(aScheme, "Scheme must not be null");
		return versionFormatter.formatVersionText(aVersion, aScheme);
	}
}
