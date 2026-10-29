package eu.algites.lib.common.enums.uiddata;

import java.util.List;
import java.util.Optional;

/**
 * Registry for {@link AIiUidEnumData} instances keyed by {@link AIiUidEnumDataType} instance and UID.
 *
 * @author linhart1
 * @date 30.01.26
 */
public interface AIiUidEnumDataRegistry {

	/**
	 * Registers the given data items.
	 * @param aReadOnly defines, if the data have to be registered as read only, co cannot be overwritten with the re-registration
	 * @param aPermanent defines, if the data have to be registered as permanent, i.e. they cannot be removed from the registry
	 *    by call of {@link #releaseData(AIiUidEnumData[])} or {@link #releaseData(AIiUidEnumDataType, String...)}
	 * @param aOverwriteExisting if the overwriting of the existing data is forced.
	 *    If it is so and the item is read-only, then {@link AIxEnumDataRegistryIllegalOperationException} is thrown.
	 *    if this parameter is false and the item exists, then it is not overwritten, no exception
	 *    is thrown, and the method returns an empty list.
	 * @param aDataToRegister data to be registered
	 * @return list of registered data items
	 * @param <T> type of the enum data expected.
	 * @param <DT> type of the enum data type expected.
	 * @throws AIxEnumDataRegistryIllegalOperationException in the case the overwrite of the read only item is forced.
	 */
	@SuppressWarnings({"unchecked", "varargs"})
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> registerData(
			boolean aReadOnly,
			boolean aPermanent,
			boolean aOverwriteExisting,
			T... aDataToRegister
	) throws AIxEnumDataRegistryIllegalOperationException;

	/**
	 * Releases the given data items.
	 * @param aDataToRelease data to be released
	 * @return list of released data items
	 * @param <T> type of the enum data expected.
	 * @param <DT> type of the enum data type expected.
	 * @throws AIxEnumDataRegistryIllegalOperationException if some of the data which should be released is marked as permanent
	 */
	@SuppressWarnings({"unchecked", "varargs"})
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> releaseData(
			T... aDataToRelease
	) throws AIxEnumDataRegistryIllegalOperationException;

	/**
	 * Releases the data items of the given data type for the given UIDs.
	 * @param aDataType data type of the items to be released
	 * @param aUidToRelease UIDs of the items to be released
	 * @return list of released data items
	 * @param <T> type of the enum data expected.
	 * @param <DT> type of the enum data type expected.
	 * @throws AIxEnumDataRegistryIllegalOperationException if some of the data which should be released is marked as permanent
	 */
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> releaseData(
			DT aDataType,
			String... aUidToRelease
	) throws AIxEnumDataRegistryIllegalOperationException;

	/**
	 * Finds the data item by UID.
	 * @param aDataType data type of the item to be found
	 * @param aUid UID of the item to be found
	 * @return optional of the found item
	 * @param <T> type of the enum data expected.
	 * @param <DT> type of the enum data type expected.
	 */
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> Optional<T> findByUid(
			DT aDataType,
			String aUid
	);

	/**
	 * Gets the data item by UID.
	 * @param aDataType data type of the item to be found
	 * @param aUid UID of the item to be found
	 * @return the found item
	 * @param <T> type of the enum data expected.
	 * @param <DT> type of the enum data type expected.
	 * @throws AIxEnumDataRegistryItemNotFoundException if the item is not found
	 */
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> T getOrThrow(
			DT aDataType,
			String aUid
	) throws AIxEnumDataRegistryItemNotFoundException;

	/**
	 * Gets all data items of the given data type.
	 * @param aDataType data type of the items to be found
	 * @return list of found items
	 * @param <DT> data type of the items to be found
	 * @param <T> type of the enum data expected.
	 */
	<DT extends AIiUidEnumDataType<?, ?>, T extends AIiUidEnumData<?, ?, DT>> List<T> items(
			DT aDataType
	);
}
