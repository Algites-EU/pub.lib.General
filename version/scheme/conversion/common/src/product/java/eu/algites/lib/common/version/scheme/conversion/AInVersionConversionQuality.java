package eu.algites.lib.common.version.scheme.conversion;

/** Quality classification for a conversion between version schemes. */
public enum AInVersionConversionQuality {
	EXACT,
	LOSSLESS_WITH_NORMALIZATION,
	LOSSY,
	UNSUPPORTED
}
