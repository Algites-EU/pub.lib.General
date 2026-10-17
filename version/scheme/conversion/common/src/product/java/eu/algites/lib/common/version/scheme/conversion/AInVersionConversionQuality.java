package eu.algites.lib.common.version.scheme.conversion;

/** Quality classification for a conversion between version schemes. */
public enum AInVersionConversionQuality {
	/**
	 * Conversion preserving the exact version requirement.
	 */
	EXACT,
	/**
	 * Conversion preserving meaning after normalizing syntax.
	 */
	LOSSLESS_WITH_NORMALIZATION,
	/**
	 * Conversion that cannot fully preserve source requirement semantics.
	 */
	LOSSY,
	/**
	 * Conversion that cannot represent the source requirement.
	 */
	UNSUPPORTED
}
