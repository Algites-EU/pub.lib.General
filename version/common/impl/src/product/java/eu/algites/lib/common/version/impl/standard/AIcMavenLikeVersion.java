package eu.algites.lib.common.version.impl.standard;


import eu.algites.lib.common.version.AIcVersion;
import eu.algites.lib.common.version.AIiVersionScheme;
import eu.algites.lib.common.version.AInBuiltinVersionScheme;
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

	/**
	 * Creates a maven like version instance with the supplied configuration.
	 * @param aOriginalText original unparsed version text
	 */
	public AIcMavenLikeVersion(@NotNull final String aOriginalText) {
		super(aOriginalText);
	}

	/** {@inheritDoc} */
	@Override
	protected AIiVersionScheme getHandlingMode() {
		return AInBuiltinVersionScheme.MAVEN_DEFAULT;
	}
}
