package eu.algites.lib.common.enums.uiddata;

import static eu.algites.lib.common.enums.uiddata.AIsUidEnumDataUtils.LAST_UID_HEADER_PART_POSITION;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>
 * Title: {@link AIiUidEnumDataType}
 * </p>
 * <p>
 * Description: Specifies the type of the enum uiddata to be parsed via the uid
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 20.01.26 8:36
 */
public interface AIiUidEnumDataType<R extends AIiUidEnumDataRecord, O extends AIiUidEnumDataOrigin> {

	/**
	 * Gets the factory for the UID parts record instance.
	 *
	 * @return the factory for the UID parts record instance
	 */
	BiFunction<String, List<String>, ? extends R> getUidRecordFactory();

	/**
	 * Gets the origin resolver for this enum uiddata type. The resolver throws an exception
	 * if the origin is unknown.
	 * @return the origin resolver. By default it resolves from {@link AInUidEnumDataOrigin#getByCodeOrThrow(String)}.
	 */
	@SuppressWarnings("unchecked")
	default Function<String, O> getOriginGetter() {
		return aS -> (O) AInUidEnumDataOrigin.getByCodeOrThrow(aS);
	}

	/**
	 * Gets the origin resolver for this enum uiddata type. The resolver throws an exception
	 * if the origin is unknown.
	 * @return the origin resolver. By default it resolves from {@link AInUidEnumDataOrigin#findByCodeOrNull(String)}.
	 */
	@SuppressWarnings("unchecked")
	default Function<String, O> getOriginFinder() {
		return aS -> (O) AInUidEnumDataOrigin.findByCodeOrNull(aS);
	}

	/**
	 * Gets the metadata for the specific UID parts of this enum uiddata type.
	 * @return the metadata for the specific uid parts
	 */
	List<AIiUidPartMetadata<O>> getSpecificUidPartsMetadata();

	/**
	 * Gets the count of UID parts for this enum uiddata type.
	 * It is equal to the count of specific parts plus 1 for origin prefix plus 1 for namespace prefix.
	 * @return the count
	 */
	default int getUidPartCount() {
		/* Count of specific parts plus origin prefix plus namespace prefix */
		return getSpecificUidPartsMetadata().size() + LAST_UID_HEADER_PART_POSITION + 1;
	}

}
