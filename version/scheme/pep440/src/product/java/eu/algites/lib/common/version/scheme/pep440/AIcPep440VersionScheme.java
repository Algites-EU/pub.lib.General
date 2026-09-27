package eu.algites.lib.common.version.scheme.pep440;

import eu.algites.lib.common.version.AIcCustomVersionScheme;
import eu.algites.lib.common.version.AIcDefaultVersionCodec;
import eu.algites.lib.common.version.AIiVersionScheme;
import eu.algites.lib.common.version.AInBuiltinVersionFormat;
import eu.algites.lib.common.version.AInBuiltinVersionStructure;

/** PEP 440 version scheme. */
public final class AIcPep440VersionScheme {
	public static final AIiVersionScheme INSTANCE = new AIcCustomVersionScheme(
			"pep440", new AIcPep440VersionComparator(), AInBuiltinVersionStructure.NO_BUILD,
			AInBuiltinVersionFormat.OMIT_BUILD, AIcDefaultVersionCodec.INSTANCE);
	private AIcPep440VersionScheme() { /* utility class */ }
}
