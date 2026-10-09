package eu.algites.lib.common.version;

import java.io.Serial;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Title: {@link AIcCalverLikeVersion}
 * </p>
 * <p>
 * Description: Immutable version representation backed by tokenization for Calver style.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public final class AIcCalverLikeVersion extends AIcVersion {

	@Serial
	private static final long serialVersionUID = 2L;

	public AIcCalverLikeVersion(@NotNull final String aOriginalText) {
		super(aOriginalText);
	}

	@Override
	protected AIiVersionScheme getHandlingMode() {
		return AInBuiltinVersionScheme.CALVER_DEFAULT;
	}

}
