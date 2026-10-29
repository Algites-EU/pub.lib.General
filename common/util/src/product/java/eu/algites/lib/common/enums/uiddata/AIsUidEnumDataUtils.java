package eu.algites.lib.common.enums.uiddata;

import eu.algites.lib.common.exception.AIxDevelopmentErrorException;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * <p>
 * Title: {@link AIsUidEnumDataUtils}
 * </p>
 * <p>
 * Description: Utilities for the Enum uiddata handling.
 *    especially for creating and parsing globally unique Identifications of the enum uiddata from and to UIDs.
 *
 * <p>UID format with i+2 specific segments (always at least 3 or more ):
 * {@code <origin-class>:<namespace>:<specific-segment-1>[:<specific-segment-2[ ... [:<specific-segment-i>]]>]}
 *
 * <ul>
 *   <li>{@code originClass} is {@code builtin} or {@code custom}</li>
 *   <li>For {@code builtin}, {@code namespace} MUST be empty</li>
 *   <li>For {@code custom}, {@code namespace} MUST be non-empty</li>
 *   <li>{@code specific-segment-i} MAY be empty or non according to the given usage and enum uiddata specification</li>
 *   <li>{@code fileType} MUST be non-empty</li>
 * </ul>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 13.01.26 17:08
 */
public class AIsUidEnumDataUtils {

	/**
	 * Separator between parts of a structured UID.
	 */
	public static final String UID_PART_SEPARATOR = ":";
	/**
	 * Pattern restricting the characters allowed in ordinary UID components.
	 */
	public static final Pattern UID_COMMON_PART_PATTERN = Pattern.compile("^[A-Za-z0-9._-]*$");
	/**
	 * Pattern requiring a nonempty, valid custom namespace.
	 */
	public static final Pattern UID_CUSTOM_NAMESPACE_PART_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
	/**
	 * Index of the origin component in a structured UID.
	 */
	public static final int ORIGIN_UID_POSITION = 0;
	/**
	 * Index of the namespace component in a structured UID.
	 */
	public static final int NAMESPACE_UID_POSITION = ORIGIN_UID_POSITION + 1;
	/**
	 * Last index occupied by the fixed UID header.
	 */
	public static final int LAST_UID_HEADER_PART_POSITION = NAMESPACE_UID_POSITION;
	/**
	 * First index available for type-specific UID components.
	 */
	public static final int FIRST_UID_SPECIFIC_PART_POSITION = LAST_UID_HEADER_PART_POSITION + 1;

	private AIsUidEnumDataUtils() {}


	/**
	 * Create a UID with specified parameters.
	 *
	 * <p>Example: {@code builtin:::jar} or {@code builtin::sources:jar}
	 *
	 * @param aOrigin origin of the UID
	 * @param aUidNamespacePart metadata for the namespace segment (empty for builtin)
	 * @param aSpecificUidParts parts of the UID after {@link #LAST_UID_HEADER_PART_POSITION} to the end
	 * @param aSpecificUidPartsMetadata metadata for the specific parts
	 * @return UID string for the builtin UID
	 * @param <O> origin type
	 */
	public static <O extends AIiUidEnumDataOrigin> String createUid(@Nonnull O aOrigin,
			String aUidNamespacePart,
			@Nonnull List<String> aSpecificUidParts,
			@Nonnull List<AIiUidPartMetadata<O>> aSpecificUidPartsMetadata) {
		if (aSpecificUidParts.size() != aSpecificUidPartsMetadata.size()) {
			throw new AIxDevelopmentErrorException("Number of specific parts must match number of metadata. Passed values:" + aSpecificUidParts + ", " + aSpecificUidPartsMetadata);
		}
		String locNormalizedNamespace;
		if (aOrigin.namespaceUsed()) {
			locNormalizedNamespace = normalizeRequiredPart(aUidNamespacePart, () -> "namespace");
			validateSafeNamespace(locNormalizedNamespace);
		}
		else {
			if (!(aUidNamespacePart == null || aUidNamespacePart.isBlank()))
				throw new AIxDevelopmentErrorException("For the origin '" + aOrigin.code() + "' cannot be used any namespace. Passed values: namespace='" + aUidNamespacePart + "', specific uid parts:"
						+ aSpecificUidParts + ", specific parts metadata: " + aSpecificUidPartsMetadata);
			locNormalizedNamespace = "";
		}
		for (int i = 0; i< aSpecificUidParts.size(); i++) {
			final String locNormalizedSpecificUidPart;
			final AIiUidPartMetadata<O> locAIiUidPartMetadata = aSpecificUidPartsMetadata.get(i);
			if (locAIiUidPartMetadata.requiredForOrigin().getOrDefault(aOrigin, false))
				locNormalizedSpecificUidPart = normalizeRequiredPart(aSpecificUidParts.get(i), locAIiUidPartMetadata.displayLabelSupplier());
			else
			  locNormalizedSpecificUidPart = normalizeOptionalPart(aSpecificUidParts.get(i));
			validateSafePart(locNormalizedSpecificUidPart, locAIiUidPartMetadata.displayLabelSupplier());

		}
		Collection<String> locResult = new ArrayList<>(aSpecificUidParts.size() + 2);
		locResult.add(aOrigin.code());
		locResult.add(locNormalizedNamespace);
		locResult.addAll(aSpecificUidParts);
		return String.join(UID_PART_SEPARATOR, locResult);
	}

	/**
	 * Create a Builtin UID with specified parameters. Uses {@link AInUidEnumDataOrigin#BUILTIN} as origin and empty namespace.
	 *
	 * <p>Example: {@code builtin:::jar} or {@code builtin::sources:jar}
	 *
	 * @param aSpecificUidParts parts of the UID after {@link #LAST_UID_HEADER_PART_POSITION} to the end
	 * @param aSpecificUidPartsMetadata metadata for the specific parts
	 * @return UID string for the builtin UID
	 */
	public static String createBuiltinUid(
			@Nonnull List<String> aSpecificUidParts,
			@Nonnull List<AIiUidPartMetadata<AInUidEnumDataOrigin>> aSpecificUidPartsMetadata) {
		return createUid(AInUidEnumDataOrigin.BUILTIN, "", aSpecificUidParts, aSpecificUidPartsMetadata);
	}

	/**
	 * Create a Builtin UID with specified parameters. Uses {@link AInUidEnumDataOrigin#BUILTIN} as origin.
	 *
	 * <p>Example: {@code builtin:::jar} or {@code builtin::sources:jar}
	 *
	 * @param aUidNamespacePart metadata for the namespace segment (empty for builtin)
	 * @param aSpecificUidParts parts of the UID after {@link #LAST_UID_HEADER_PART_POSITION} to the end
	 * @param aSpecificUidPartsMetadata metadata for the specific parts
	 * @return UID string for the builtin UID
	 */
	public static String createCustomUid(
			String aUidNamespacePart,
			@Nonnull List<String> aSpecificUidParts,
			@Nonnull List<AIiUidPartMetadata<AInUidEnumDataOrigin>> aSpecificUidPartsMetadata) {
		return createUid(AInUidEnumDataOrigin.BUILTIN, aUidNamespacePart, aSpecificUidParts, aSpecificUidPartsMetadata);
	}

	/**
	 * Parse and validate an OutputType UID.
	 *
	 * @param aEnumDataType enum uiddata type for which the result has to be returned
	 * @param aUid uid string
	 * @return parsed parts
	 * @throws IllegalArgumentException if invalid
	 * @param <R> type of the result enum uiddata type expected.
	 * @param <O> origin type
	 * @param <GUEDT> type of the globally unique enum uiddata type
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> R parseUid(
			@Nonnull GUEDT aEnumDataType,
			@Nonnull String aUid) {
		Objects.requireNonNull(aEnumDataType, () -> "Enum uiddata type must not be null. Passed UID:" + aUid);
		String locUid = Objects.requireNonNull(aUid, () -> "aUid must not be null. Passed enum uiddata type:" + aEnumDataType);
		String[] locParts = splitUidIntoParts(aEnumDataType, locUid);
		O locOrigin = aEnumDataType.getOriginGetter().apply(locParts[ORIGIN_UID_POSITION]);

		validatePartsSemantics(aEnumDataType, locOrigin, locParts);

		return aEnumDataType.getUidRecordFactory().apply(aUid, List.of(locParts));
	}

	/**
	 * Validate a UID (throws on error).
	 *
	 * @param aEnumDataType component type to be examined
	 * @param aUid uid string
	 * @throws IllegalArgumentException if invalid
	 * @param <R> type of the enum uiddata type expected.
	 * @param <O> origin type
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> void validateUid(
			@Nonnull final GUEDT aEnumDataType, @Nonnull String aUid)
			throws IllegalArgumentException {
		parseUid(aEnumDataType, aUid);
	}

	/**
	 * @param aEnumDataType enum uiddata type to be examined
	 * @param aUid uid string
	 * @return true if valid, else false
	 * @param <R> type of the enum uiddata type expected.
	 * @param <O> origin type
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>>
	boolean isValidOutputTypeUid(
			@Nonnull final GUEDT aEnumDataType,
			@Nonnull String aUid) {
		try {
			validateUid(aEnumDataType, aUid);
			return true;
		} catch (RuntimeException locException) {
			return false;
		}
	}

	/**
	 * @param aEnumDataType component type to be examined
	 * @param aUid uid string
	 * @return kind class
	 * @param <R> type of the enum uiddata type expected.
	 * @param <O> origin type
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> AIiUidEnumDataOrigin getOrigin(
			@Nonnull final GUEDT aEnumDataType,
			@Nonnull String aUid) {
		return parseUid(aEnumDataType, aUid).origin();
	}

	/**
	 * @param aComponentType component type to be examined
	 * @param aUid uid string
	 * @return namespace segment (empty for builtin)
	 * @param <R> type of the enum uiddata type expected.
	 * @param <O> origin type
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> String getNamespace(
			final GUEDT aComponentType, String aUid) {
		return parseUid(aComponentType, aUid).namespace();
	}

	/**
	 * Split a UID into parts.
	 * @param aEnumDataType enum uiddata type to be examined
	 * @param aUid uid string
	 * @return parts split according to the definition of the enum uiddata type
	 * @param <R> type of the enum uiddata type expected.
	 * @param <O> origin type
	 * @throws IllegalArgumentException if uid is invalid
	 */
	public static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> String[] splitUidIntoParts(
			@Nonnull final GUEDT aEnumDataType,
			@Nonnull final String aUid)
	throws IllegalArgumentException {
		int locFirst = aUid.indexOf(UID_PART_SEPARATOR);
		if (locFirst < 0) {
			throw new IllegalArgumentException("Invalid UID (missing ':' separators): '" + aUid + "'");
		}
		String[] locParts = aUid.split(UID_PART_SEPARATOR, -1);
		if (locParts.length != aEnumDataType.getUidPartCount()) {
			throw new IllegalArgumentException(
					"Invalid UID (expected " + aEnumDataType.getUidPartCount()
							+ " segments '<origin-code>:<namespace>:"
							+ String.join(UID_PART_SEPARATOR,
														aEnumDataType.getSpecificUidPartsMetadata().stream()
															.map(locName -> "<" + locName + ">").toList()) + "': '" + aUid + "'"
			);
		}
		return locParts;
	}

	private static <R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin,
			GUEDT extends AIiUidEnumDataType<? extends R, O>> void validatePartsSemantics(
			@Nonnull GUEDT aEnumDataType,
			@Nonnull O aEnumDataOrigin,
			@Nonnull String[] aEnumDataParts
	) {
		if (aEnumDataParts.length != aEnumDataType.getUidPartCount()) {
			throw new IllegalArgumentException(
					"Invalid UID (expected " + aEnumDataType.getUidPartCount()
							+ " segments but resolved " + aEnumDataParts.length + " from Uid '" + java.lang.String.join(UID_PART_SEPARATOR, aEnumDataParts) + "'"
			);
		}
		String locNamespace = Objects.requireNonNull(aEnumDataParts[NAMESPACE_UID_POSITION], "namespace segment must not be null");

		if (!aEnumDataOrigin.namespaceUsed()) {
			if (!locNamespace.isEmpty()) {
				throw new IllegalArgumentException("Invalid builtin UID: " 
						+ aEnumDataType.getSpecificUidPartsMetadata().get(NAMESPACE_UID_POSITION).displayLabelSupplier().get()
						+ " must be empty");
			}
		} else {
			if (locNamespace.isBlank()) {
				throw new IllegalArgumentException("Invalid builtin UID: "
						+ aEnumDataType.getSpecificUidPartsMetadata().get(NAMESPACE_UID_POSITION).displayLabelSupplier().get()
						+ " must be non-blank");
			}
			validateSafeNamespace(locNamespace);
		}
		for (int i = FIRST_UID_SPECIFIC_PART_POSITION; i < aEnumDataType.getUidPartCount(); i++) {
			final AIiUidPartMetadata<O> locPartMetadata = aEnumDataType.getSpecificUidPartsMetadata().get(i - FIRST_UID_SPECIFIC_PART_POSITION);
			Objects.requireNonNull(aEnumDataParts[i], () -> "<" + locPartMetadata.displayLabelSupplier().get() + "> part must not be null");
			validateSafePart(aEnumDataParts[i], locPartMetadata.displayLabelSupplier());
		}
	}

	private static void validateSafePart(CharSequence aSegment, Supplier<String> aPartDisplayLabelSupplier) {
		if (!UID_COMMON_PART_PATTERN.matcher(aSegment).matches()) {
			throw new IllegalArgumentException(
					"Invalid " + aPartDisplayLabelSupplier.get() + " part '" + aSegment + "': allowed pattern is " + UID_COMMON_PART_PATTERN.pattern()
			);
		}
	}

	private static void validateSafeNamespace(CharSequence aNamespace) {
		if (!UID_CUSTOM_NAMESPACE_PART_PATTERN.matcher(aNamespace).matches()) {
			throw new IllegalArgumentException(
					"Invalid namespace '" + aNamespace + "': allowed pattern is " + UID_CUSTOM_NAMESPACE_PART_PATTERN.pattern()
			);
		}
	}

	private static String normalizeOptionalPart(String aValue) {
		if (aValue == null) {
			return "";
		}
		String locTrimmedValue = aValue.trim();
		return locTrimmedValue;
	}

	private static String normalizeRequiredPart(String aValue, Supplier<String> aPartDisplayLabelSupplier) {
		String locValue = Objects.requireNonNull(aValue, () -> aPartDisplayLabelSupplier.get() + " must not be null").trim();
		if (locValue.isEmpty()) {
			throw new IllegalArgumentException(aPartDisplayLabelSupplier.get() + " must be non-empty");
		}
		return locValue;
	}

}
