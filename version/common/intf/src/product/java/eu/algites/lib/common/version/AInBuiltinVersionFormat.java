package eu.algites.lib.common.version;

/**
 * <p>
 * Title: {@link AInBuiltinVersionFormat}
 * </p>
 * <p>
 * Description: Built-in {@link AIiVersionFormat} definitions.
 * </p>
 *
 * @author linhart1
 * @date 28.01.26
 */
public enum AInBuiltinVersionFormat implements AIiVersionFormat {

	/**
	 * Default format: emits build metadata if structure has it.
	 */
	EMIT_BUILD("emit-build", AInVersionBuildFormatPolicy.EMIT),

	/**
	 * Omits build metadata even if present in the input.
	 */
	OMIT_BUILD("omit-build", AInVersionBuildFormatPolicy.OMIT),

	/**
	 * Maps build metadata into qualifier/prerelease section.
	 */
	MAP_BUILD_TO_QUALIFIER("map-build-to-qualifier", AInVersionBuildFormatPolicy.MAP_TO_QUALIFIER),
  ;

	private final String code;
	private final AInVersionBuildFormatPolicy buildFormatPolicy;

	AInBuiltinVersionFormat(final String aCode, final AInVersionBuildFormatPolicy aBuildFormatPolicy) {
		code = aCode;
		buildFormatPolicy = aBuildFormatPolicy;
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public AInVersionBuildFormatPolicy buildFormatPolicy() {
		return buildFormatPolicy;
	}
}
