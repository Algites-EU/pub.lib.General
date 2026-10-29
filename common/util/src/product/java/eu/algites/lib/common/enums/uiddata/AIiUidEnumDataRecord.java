package eu.algites.lib.common.enums.uiddata;

/**
 * <p>
 * Title: {@link AIiUidEnumDataRecord}
 * </p>
 * <p>
 * Description: General interface for the Uid records,
 * containing the Uid and the split parts of the records.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2026 Artur Linhart, Algites
 * </p>
 * <p>
 * Company: Algites
 * </p>
 *
 * @author linhart1
 * @date 18.01.26 15:46
 */
public interface AIiUidEnumDataRecord {

	/**
	 * Unique identifier of the record
	 * @return unique identifier
	 */
	String uid();

	/**
	 * The component of the Uid, returned by {@link #uid()} method,
	 * siding on the position {@link AIsUidEnumDataUtils#ORIGIN_UID_POSITION}
	 * @return origin (builtin/custom)
	 */
	AIiUidEnumDataOrigin origin();

	/**
	 * The component of the Uid, returned by {@link #uid()} method,
	 * siding on the position {@link AIsUidEnumDataUtils#ORIGIN_UID_POSITION}
	 * @return namespace segment (empty for origins, where {@link AIiUidEnumDataOrigin#namespaceUsed()}
	 *    return false and not empty otherwise)
	 */
	String namespace();
}
