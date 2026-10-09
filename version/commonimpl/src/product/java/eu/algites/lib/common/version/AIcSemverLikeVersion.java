package eu.algites.lib.common.version;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIcSemverLikeVersion}
 * </p>
 * <p>
 * Description: Immutable version representation backed by tokenization for Semver style.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcSemverLikeVersion extends AIcVersion {

	@Serial
	private static final long serialVersionUID = 2L;

	public AIcSemverLikeVersion(@NotNull final String aOriginalText) {
		super(aOriginalText);
	}

	@Override
	protected AIiVersionScheme getHandlingMode() {
		return AInBuiltinVersionScheme.SEMVER_DEFAULT;
	}

}
