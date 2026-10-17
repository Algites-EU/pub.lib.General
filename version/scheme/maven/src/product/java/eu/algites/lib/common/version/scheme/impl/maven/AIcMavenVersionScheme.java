package eu.algites.lib.common.version.scheme.impl.maven;

import eu.algites.lib.common.version.impl.standard.AIcCustomVersionScheme;
import eu.algites.lib.common.version.AIcDefaultVersionCodec;
import eu.algites.lib.common.version.AIcMavenLikeVersionComparator;
import eu.algites.lib.common.version.AIiVersionScheme;
import eu.algites.lib.common.version.AInBuiltinVersionFormat;
import eu.algites.lib.common.version.AInBuiltinVersionStructure;

/**
 * Canonical Maven-compatible version scheme exposed by the version library.
 */
public final class AIcMavenVersionScheme {

	/**
	 * Shared immutable instance of the maven version scheme implementation.
	 */
	public static final AIiVersionScheme INSTANCE = new AIcCustomVersionScheme(
			"maven",
			new AIcMavenLikeVersionComparator(),
			AInBuiltinVersionStructure.NO_BUILD,
			AInBuiltinVersionFormat.OMIT_BUILD,
			AIcDefaultVersionCodec.INSTANCE
	);

	private AIcMavenVersionScheme() {
		/* utility class */
	}
}
