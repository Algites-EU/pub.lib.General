package eu.algites.lib.common.version;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIcMavenLikeVersion}
 * </p>
 * <p>
 * Description: Immutable version representation backed by tokenization for Maven style.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcMavenLikeVersion extends AIcVersion {

	@Serial
	private static final long serialVersionUID = 2L;

	public AIcMavenLikeVersion(@NotNull final String aOriginalText) {
		super(aOriginalText);
	}

	@Override
	protected AIiVersionScheme getHandlingMode() {
		return AInBuiltinVersionScheme.MAVEN_DEFAULT;
	}
}
