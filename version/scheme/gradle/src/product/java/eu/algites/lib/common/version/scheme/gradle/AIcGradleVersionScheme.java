package eu.algites.lib.common.version.scheme.gradle;

import eu.algites.lib.common.version.AIcCustomVersionScheme;
import eu.algites.lib.common.version.AIcDefaultVersionCodec;
import eu.algites.lib.common.version.AIcMavenLikeVersionComparator;
import eu.algites.lib.common.version.AIiVersionScheme;
import eu.algites.lib.common.version.AInBuiltinVersionFormat;
import eu.algites.lib.common.version.AInBuiltinVersionStructure;

/**
 * Gradle dependency-version scheme.
 *
 * The explicit rich-constraint model is represented by {@link AIrGradleVersionConstraint}.
 */
public final class AIcGradleVersionScheme {

	public static final AIiVersionScheme INSTANCE = new AIcCustomVersionScheme(
			"gradle",
			new AIcMavenLikeVersionComparator(),
			AInBuiltinVersionStructure.NO_BUILD,
			AInBuiltinVersionFormat.OMIT_BUILD,
			AIcDefaultVersionCodec.INSTANCE
	);

	private AIcGradleVersionScheme() {
		/* utility class */
	}
}
