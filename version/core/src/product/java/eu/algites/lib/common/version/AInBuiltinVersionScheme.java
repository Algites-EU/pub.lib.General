package eu.algites.lib.common.version;

import static eu.algites.lib.common.enums.uiddata.AInUidEnumDataOrigin.BUILTIN;
import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.createBuiltinUid;
import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.parseUid;

import eu.algites.lib.common.enums.uiddata.AInUidEnumDataOrigin;
import eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.gradle.internal.impldep.com.fasterxml.jackson.annotation.JsonCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Title: {@link AInBuiltinVersionScheme}
 * </p>
 * <p>
 * Description: Common built-in version schemes defining how version precedence is computed and
 * how the version string is structurally interpreted (build delimiter/order) and formatted.
 * </p>
 * <p>
 * A version scheme is a composite of:
 * </p>
 * <ul>
 *   <li>A {@link AIiVersionComparator} used for precedence comparisons.</li>
 *   <li>A {@link AIiVersionStructure} defining how the textual form is structurally interpreted
 *       (e.g. build delimiter, whether version comes before build, build comparison policy).</li>
 *   <li>A {@link AIiVersionFormat} defining how the version is formatted back to text
 *       (e.g. whether build metadata is emitted, omitted or mapped).</li>
 * </ul>
 * <p>
 * Built-in items are identified by their {@link #schemeCode()} only. Structure/format are not part of the UID,
 * so the config can continue using the scheme UID without needing separate UIDs for structure/format.
 * </p>
 *
 * @author linhart1
 * @date 26.01.26
 */
public enum AInBuiltinVersionScheme implements AIiVersionSchemeData {

	/**
	 * Practical Maven/Gradle-friendly ordering, tolerant to common patterns.
	 */
	MAVEN_DEFAULT(
			"maven-default",
			new AIcMavenLikeVersionComparator(),
			AInBuiltinVersionStructure.NO_BUILD,
			AInBuiltinVersionFormat.OMIT_BUILD
	),

	MAVEN_BUILD_METADATA_IGNORED(
			"maven-build-metadata-ignored",
			new AIcMavenLikeVersionComparator(),
			AInBuiltinVersionStructure.BUILD_AFTER_PLUS_IGNORED,
			AInBuiltinVersionFormat.MAP_BUILD_TO_QUALIFIER
	),

	/**
	 * Semantic Versioning precedence.
	 * Build metadata after '+' is ignored for precedence comparisons but can be emitted in formatted output.
	 */
	SEMVER_DEFAULT(
			"semver-default",
			new AIcSemverLikeVersionComparator(),
			AInBuiltinVersionStructure.BUILD_AFTER_PLUS_IGNORED,
			AInBuiltinVersionFormat.EMIT_BUILD
	),

	SEMVER_BUILD_FIRST(
			"semver-build-first",
			new AIcSemverLikeVersionComparator(),
			AInBuiltinVersionStructure.BUILD_BEFORE_PLUS_IGNORED,
			AInBuiltinVersionFormat.EMIT_BUILD
	),

	SEMVER_BUILD_ORDERED(
			"semver-build-ordered",
			new AIcSemverLikeVersionComparator(),
			AInBuiltinVersionStructure.BUILD_AFTER_PLUS_ORDERED,
			AInBuiltinVersionFormat.EMIT_BUILD
	),

	/**
	 * Example calendar-versioning comparator.
	 */
	CALVER_DEFAULT(
			"calver-default",
			new AIcCalverLikeVersionComparator(),
			AInBuiltinVersionStructure.NO_BUILD,
			AInBuiltinVersionFormat.OMIT_BUILD
	);

	private final String code;
	private final AIiVersionComparator versionComparator;
	private final AIiVersionStructure versionStructure;
	private final AIiVersionFormat versionFormat;
	private final AIiVersionCodec versionCodec;
	private final String uid;

	private static final AIiVersionSchemeDataType DATA_TYPE = new AIcVersionSchemeDataType();

	AInBuiltinVersionScheme(
			final String aCode,
			final AIiVersionComparator aVersionComparator,
			final AIiVersionStructure aVersionStructure,
			final AIiVersionFormat aVersionFormat
	) {
		code = Objects.requireNonNull(aCode, "code");
		versionComparator = Objects.requireNonNull(aVersionComparator, "versionComparator");
		versionStructure = Objects.requireNonNull(aVersionStructure, "versionStructure");
		versionFormat = Objects.requireNonNull(aVersionFormat, "versionFormat");
		versionCodec = AIcDefaultVersionCodec.INSTANCE;

		uid = AIsUidEnumDataUtils.createBuiltinUid(
				List.of(code),
				AIiVersionSchemeDataUidRecord.RECORD_SPECIFIC_PARTS_METADATA);
	}

	@Override
	public String code() {
		return code;
	}

	@Override
	public AIiVersionComparator versionComparator() {
		return versionComparator;
	}

	@Override
	public AIiVersionStructure versionStructure() {
		return versionStructure;
	}

	@Override
	public AIiVersionFormat versionFormat() {
		return versionFormat;
	}

	@Override
	public @NotNull AIiVersionCodec versionCodec() {
		return versionCodec;
	}

	@Override
	public AIiVersionSchemeDataType getDataType() {
		return DATA_TYPE;
	}

	@Override
	public String uid() {
		return uid;
	}

	@Override
	public AInUidEnumDataOrigin origin() {
		return AInUidEnumDataOrigin.BUILTIN;
	}

	@Override
	public String namespace() {
		return "";
	}

	@Override
	public String schemeCode() {
		return code();
	}

	/**
	 * @param aCode code to be searched for
	 * @return the enum value
	 * @throws IllegalArgumentException if the code is not null but unknown
	 */
	@JsonCreator
	public static AInBuiltinVersionScheme getByCodeOrThrow(final String aCode) throws IllegalArgumentException {
		final AInBuiltinVersionScheme locValue = findByCode(aCode);
		if (locValue != null) {
			return locValue;
		}
		throw new IllegalArgumentException("Unknown version scheme: " + aCode);
	}

	/**
	 * @param aCode code to be searched for
	 * @return the enum value or null if not found
	 */
	public static @Nullable AInBuiltinVersionScheme findByCode(final String aCode) {
		for (AInBuiltinVersionScheme locValue : values()) {
			if (locValue.code().equalsIgnoreCase(aCode)) {
				return locValue;
			}
		}
		return null;
	}

	/**
	 * Gets the {@link AInBuiltinVersionScheme} by its properties.
	 *
	 * @param aOrigin the origin
	 * @param aNamespace the namespace
	 * @param aSchemeCode the scheme code (last UID component)
	 * @return the found scheme or throws an exception if not found
	 * @throws IllegalArgumentException if origin is not {@link AInUidEnumDataOrigin#BUILTIN}
	 *                                  or {@link #getByUidOrThrow(String)} throws an exception
	 */
	public static AInBuiltinVersionScheme getByPropsOrThrow(
			final AInUidEnumDataOrigin aOrigin,
			final String aNamespace,
			final String aSchemeCode) throws IllegalArgumentException {
		if (aOrigin != BUILTIN) {
			throw new IllegalArgumentException("Unsupported origin: '" + aOrigin + "' for parameters namespace='"
					+ aNamespace + "', scheme-code='" + aSchemeCode + "'");
		}
		return getByUidOrThrow(createBuiltinUid(
				List.of(aSchemeCode),
				AIiVersionSchemeDataUidRecord.RECORD_SPECIFIC_PARTS_METADATA));
	}

	/**
	 * Gets the {@link AInBuiltinVersionScheme} by its UID.
	 *
	 * @param aUid scheme UID
	 * @return the found scheme or throws an exception if not found
	 * @throws IllegalArgumentException if the UID is invalid or not found among enum values
	 */
	public static AInBuiltinVersionScheme getByUidOrThrow(final String aUid) throws IllegalArgumentException {
		return findByUid(aUid)
				.orElseThrow(() -> new IllegalArgumentException("Illegal version scheme UID: '" + aUid + "'"));
	}

	/**
	 * Finds the given builtin by the builtin-UID.
	 *
	 * @param aUid UID, for which the built-in item has to be searched for
	 * @return the found built-in item or empty optional, if no built-in item was found
	 */
	public static Optional<AInBuiltinVersionScheme> findByUid(final String aUid) {
		if (aUid == null || aUid.isBlank()) {
			return Optional.empty();
		}
		AIiVersionSchemeDataUidRecord locParsedRecord = parseUid(
				DATA_TYPE, aUid);
		if (locParsedRecord.origin() != BUILTIN) {
			return Optional.empty();
		}
		return Stream.of(values())
				.filter(locItem -> locItem.code().equals(locParsedRecord.code()))
				.findAny();
	}

}
