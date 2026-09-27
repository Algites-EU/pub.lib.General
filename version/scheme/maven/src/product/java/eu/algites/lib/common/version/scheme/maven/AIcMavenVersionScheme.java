package eu.algites.lib.common.version.scheme.maven;

import eu.algites.lib.common.version.AIcCustomVersionScheme;
import eu.algites.lib.common.version.AIcDefaultVersionCodec;
import eu.algites.lib.common.version.AIcMavenLikeVersionComparator;
import eu.algites.lib.common.version.AIiVersionScheme;
import eu.algites.lib.common.version.AInBuiltinVersionFormat;
import eu.algites.lib.common.version.AInBuiltinVersionStructure;

/**
 * Canonical Maven-compatible version scheme exposed by the version library.
 */
public final class AIcMavenVersionScheme {

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
